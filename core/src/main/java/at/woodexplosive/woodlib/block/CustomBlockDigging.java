package at.woodexplosive.woodlib.block;

import at.woodexplosive.woodlib.Scheduler;
import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.block.CustomBlockPart;
import at.woodexplosive.woodlib.api.block.DisplayDefinition;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgeBlockChanges;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drives timed mining (see {@link ICustomBlock#hardness()}) off the raw {@code PLAYER_DIGGING} packet
 * instead of any Bukkit event, because a placed part is {@link org.bukkit.Material#BARRIER Barrier} -
 * unbreakable in survival - so vanilla clients never send {@code FINISHED_DIGGING} for it themselves.
 * That leaves exactly {@code START_DIGGING} (mouse down) and {@code CANCELLED_DIGGING} (mouse up, look
 * away, switch item, take damage, ...) as clean start/stop signals, and lets the server stay fully
 * authoritative over how long breaking actually takes.
 *
 * <p>Packet listeners run on the Netty thread, never the main thread - every reaction here hops back via
 * {@link at.woodexplosive.woodlib.Scheduler#next(Runnable)} before touching the Bukkit API.</p>
 *
 * <p>{@link ICustomBlock#hardness()} {@code <= 0} CustomBlocks are untouched by this class; they keep
 * breaking instantly via {@link CustomBlockListener}'s {@code LEFT_CLICK_BLOCK} handling.</p>
 */
public final class CustomBlockDigging implements Listener {

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    /**
     * Registers the raw packet listener driving digging sessions. Call once, alongside registering an
     * instance of this class as a normal Bukkit {@link Listener} for quit cleanup.
     */
    public static void registerPacketListener() {
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract() {
            @Override
            public void onPacketReceive(@NotNull PacketReceiveEvent event) {
                if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;

                Player player = event.getPlayer();
                if (player == null) return;

                WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
                Vector3i pos = wrapper.getBlockPosition();
                int x = pos.getX(), y = pos.getY(), z = pos.getZ();

                // Simple getter, not a mutation - safe off the main thread. Cancelling has to happen
                // synchronously here, before this method returns: by the time a hop to the main thread
                // (Scheduler.next) would come back, PacketEvents has already forwarded the packet on to
                // Paper's own vanilla digging handler.
                if (CustomBlockRuntime.isKnownPart(player.getWorld().getUID(), x, y, z)) {
                    event.setCancelled(true);

                    // Every digging packet carries a sequence number the client uses to reconcile its own
                    // predicted block state; vanilla always acks it, even on a no-op. Skipping this for a
                    // cancelled packet leaves the client's digging state machine waiting indefinitely - for
                    // a real (non-Barrier) barrierMaterial() the client actively predicts progress/breaking
                    // and, never hearing back, treats the dig as stuck and keeps aborting/restarting it.
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player,
                            new WrapperPlayServerAcknowledgeBlockChanges(wrapper.getSequence()));
                }

                DiggingAction action = wrapper.getAction();
                if (action != DiggingAction.START_DIGGING && action != DiggingAction.CANCELLED_DIGGING) return;

                UUID uuid = player.getUniqueId();

                if (action == DiggingAction.START_DIGGING) {
                    Scheduler.next(() -> startDigging(player, x, y, z));
                } else {
                    Scheduler.next(() -> cancelSession(uuid, true));
                }
            }
        });
    }

    /**
     * Cleans up any in-progress session on disconnect, so a player leaving mid-dig doesn't leave a
     * dangling repeating task behind.
     * @param event the quit event
     */
    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        cancelSession(event.getPlayer().getUniqueId(), false);
    }

    /**
     * Starts (or, for a stale leftover, restarts) a digging session for a {@code START_DIGGING} packet
     * targeting a timed CustomBlock part, or breaks it immediately if {@link CustomBlockMining#breakTicks}
     * comes back non-positive (e.g. an environment/effect combo fast enough to be instant anyway).
     * @param player the digging player
     * @param x the targeted block's x
     * @param y the targeted block's y
     * @param z the targeted block's z
     */
    private static void startDigging(@NotNull Player player, int x, int y, int z) {
        Block block = player.getWorld().getBlockAt(x, y, z);
        Plugin plugin = WoodLib.plugin();
        CustomBlockRuntime.ResolvedPart resolved = CustomBlockRuntime.resolve(block, plugin);
        if (resolved == null) return;

        ICustomBlock customBlock = resolved.customBlock();
        if (customBlock.hardness() <= 0f) return; // instant-break path (CustomBlockListener) handles this

        GameMode gameMode = player.getGameMode();
        if (gameMode == GameMode.SPECTATOR || gameMode == GameMode.ADVENTURE) return;

        cancelSession(player.getUniqueId(), false);

        // Creative always insta-mines, exactly like vanilla - never run it through the timed formula.
        if (gameMode == GameMode.CREATIVE) {
            CustomBlockListener.performBreak(player, resolved, block);
            return;
        }

        int totalTicks = CustomBlockMining.breakTicks(customBlock, player);
        if (totalTicks <= 0) {
            CustomBlockListener.performBreak(player, resolved, block);
            return;
        }

        Session session = new Session(resolved, block, totalTicks);
        session.task = Scheduler.repeat(() -> tick(player, session), 1, 1);
        SESSIONS.put(player.getUniqueId(), session);
    }

    /**
     * How often (in ticks) crack particles burst while digging - every tick would be particle spam.
     */
    private static final int PARTICLE_INTERVAL_TICKS = 4;

    /**
     * Advances one active session by a tick: updates the crack overlay and (periodically) crack
     * particles, and completes the break once enough ticks have elapsed. Bails (clearing the session) if
     * the target stopped being a valid part - e.g. broken by another player, an explosion, or the
     * definition was unregistered mid-dig.
     * @param player the digging player
     * @param session the session being advanced
     */
    private static void tick(@NotNull Player player, @NotNull Session session) {
        if (!player.isOnline() || CustomBlockRuntime.resolve(session.block, WoodLib.plugin()) == null) {
            cancelSession(player.getUniqueId(), false);
            return;
        }

        session.elapsed++;
        int stage = (int) Math.min(9, (session.elapsed * 10L) / session.totalTicks);
        sendBreakAnimation(player, session.block, (byte) stage);

        if (session.elapsed % PARTICLE_INTERVAL_TICKS == 0) {
            spawnCrackParticles(session.block, session.resolved.customBlock(), session.resolved.partIndex());
        }

        if (session.elapsed >= session.totalTicks) {
            SESSIONS.remove(player.getUniqueId());
            session.task.cancel();
            CustomBlockListener.performBreak(player, session.resolved, session.block);
        }
    }

    /**
     * Bursts a few world-visible crack particles at {@code block}, textured after whichever display the
     * clicked {@link CustomBlockPart} actually renders - the part's own material is a real
     * {@link org.bukkit.Material#BARRIER Barrier} (or similar), which the client renders as nothing, so
     * particles keyed to it would be invisible too. Falls back to doing nothing if the part has no
     * displays to source an appearance from.
     * @param block the part block being mined
     * @param customBlock the CustomBlock definition
     * @param partIndex the clicked part's index into {@link ICustomBlock#parts()}
     */
    private static void spawnCrackParticles(@NotNull Block block, @NotNull ICustomBlock customBlock, int partIndex) {
        List<CustomBlockPart> parts = customBlock.parts();
        if (partIndex < 0 || partIndex >= parts.size()) return;

        List<DisplayDefinition> displays = parts.get(partIndex).displays();
        if (displays.isEmpty()) return;

        World world = block.getWorld();
        Location center = block.getLocation().add(0.5, 0.5, 0.5);

        switch (displays.getFirst()) {
            case DisplayDefinition.OfBlock ofBlock ->
                    world.spawnParticle(Particle.BLOCK, center, 3, 0.25, 0.25, 0.25, 0, ofBlock.blockData());
            case DisplayDefinition.OfItem ofItem ->
                    world.spawnParticle(Particle.ITEM, center, 3, 0.25, 0.25, 0.25, 0, ofItem.itemStack());
        }
    }

    /**
     * Cancels {@code uuid}'s active session, if any, and stops its repeating task.
     * @param uuid the digging player's id
     * @param sendClearPacket whether to tell the client to clear the crack overlay (skip this when the
     *                        block is about to disappear anyway, e.g. on completion)
     */
    private static void cancelSession(@NotNull UUID uuid, boolean sendClearPacket) {
        Session session = SESSIONS.remove(uuid);
        if (session == null) return;
        session.task.cancel();

        if (sendClearPacket) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) sendBreakAnimation(player, session.block, (byte) 10);
        }
    }

    /**
     * Sends the vanilla crack-overlay packet for {@code block}, visible only to {@code player}.
     * @param player the player to show the overlay to
     * @param block the block to overlay
     * @param stage 0-9 for a progress stage, or any value outside that range to clear the overlay
     */
    private static void sendBreakAnimation(@NotNull Player player, @NotNull Block block, byte stage) {
        Vector3i pos = new Vector3i(block.getX(), block.getY(), block.getZ());
        WrapperPlayServerBlockBreakAnimation packet = new WrapperPlayServerBlockBreakAnimation(player.getEntityId(), pos, stage);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    /** One player's in-progress dig against one CustomBlock part. */
    private static final class Session {
        final CustomBlockRuntime.ResolvedPart resolved;
        final Block block;
        final int totalTicks;
        int elapsed;
        BukkitTask task;

        Session(@NotNull CustomBlockRuntime.ResolvedPart resolved, @NotNull Block block, int totalTicks) {
            this.resolved = resolved;
            this.block = block;
            this.totalTicks = totalTicks;
        }
    }
}
