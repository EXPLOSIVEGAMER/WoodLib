package at.woodexplosive.woodlib.impl.block;

import at.woodexplosive.woodlib.api.block.CustomBlockRegistry;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.PlacementContext;
import at.woodexplosive.woodlib.api.utils.BlockPos;
import at.woodexplosive.woodlib.impl.Scheduler;
import at.woodexplosive.woodlib.impl.interfaces.Internal;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Server-side counterpart of HBM's {@code drawPlacementHighlight}: while a player holds an item linked
 * to an {@link ICustomBlock} with {@link ICustomBlock#showPlacementPreview()}, outlines where it would be
 * placed with dust particles only that player sees - green if {@link ICustomBlock#canPlace} passes, red
 * otherwise, ports in blue.
 */
@Internal
public final class CustomBlockPlacementPreview {

    /** How often the preview refreshes, in ticks. Dust particles live long enough to bridge the gap. */
    private static final long INTERVAL_TICKS = 4;
    /** Reach for the targeted block, matching the vanilla survival block interaction range. */
    private static final int REACH = 5;
    /** Distance between particles along an outline edge, in blocks. */
    private static final double STEP = 0.25;

    private static final Particle.DustOptions VALID = new Particle.DustOptions(Color.fromRGB(0x40FF40), 0.6f);
    private static final Particle.DustOptions INVALID = new Particle.DustOptions(Color.fromRGB(0xFF4040), 0.6f);
    private static final Particle.DustOptions PORT = new Particle.DustOptions(Color.fromRGB(0x4080FF), 0.6f);

    private CustomBlockPlacementPreview() {}

    /** Starts the preview task. Call once from {@code WoodLib.init}. */
    public static void start() {
        Scheduler.repeat(CustomBlockPlacementPreview::tick, INTERVAL_TICKS, INTERVAL_TICKS);
    }

    private static void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GameMode gameMode = player.getGameMode();
            if (gameMode == GameMode.SPECTATOR || gameMode == GameMode.ADVENTURE) continue;

            NamespacedKey linkedId = ICustomBlock.linkedIdOf(player.getInventory().getItemInMainHand());
            if (linkedId == null) continue;

            ICustomBlock customBlock = CustomBlockRegistry.get(linkedId);
            if (customBlock == null || !customBlock.showPlacementPreview()) continue;

            Block clicked = player.getTargetBlockExact(REACH);
            BlockFace face = player.getTargetBlockFace(REACH);
            if (clicked == null || face == null || !face.isCartesian()) continue;

            PlacementContext placement = CustomBlockRuntime.computePlacement(customBlock, player, clicked, face);
            boolean valid = customBlock.canPlace(placement);

            drawOutline(player, placement.instance().cells(), valid ? VALID : INVALID);
            drawOutline(player, placement.instance().portBlocks(), PORT);
        }
    }

    /**
     * Draws the outer edges of the union of {@code blocks}: an edge of a cell is skipped wherever a
     * neighbouring cell continues the surface, the same rule HBM's outline uses.
     */
    private static void drawOutline(@NotNull Player player, @NotNull List<Block> blocks, @NotNull Particle.DustOptions color) {
        Set<BlockPos> set = new HashSet<>();
        for (Block block : blocks) set.add(BlockPos.of(block));

        for (BlockPos pos : set) {
            int x = pos.getX(), y = pos.getY(), z = pos.getZ();
            // Each of a cube's 12 edges is shared by two faces; draw it if both faces are exposed
            // (convex corner) or both neighbours exist but the diagonal doesn't (concave corner).
            for (int a = 0; a <= 1; a++) {
                for (int b = 0; b <= 1; b++) {
                    int da = a == 0 ? -1 : 1, db = b == 0 ? -1 : 1;
                    // edges along X: varying y (a) and z (b)
                    if (isEdge(set, pos, 0, da, 0, 0, 0, db)) line(player, color, x, y + a, z + b, x + 1, y + a, z + b);
                    // edges along Y: varying x (a) and z (b)
                    if (isEdge(set, pos, da, 0, 0, 0, 0, db)) line(player, color, x + a, y, z + b, x + a, y + 1, z + b);
                    // edges along Z: varying x (a) and y (b)
                    if (isEdge(set, pos, da, 0, 0, 0, db, 0)) line(player, color, x + a, y + b, z, x + a, y + b, z + 1);
                }
            }
        }
    }

    /**
     * @return whether the edge of {@code pos} between its faces towards {@code (ax, ay, az)} and
     *         {@code (bx, by, bz)} lies on the outline of {@code set}
     */
    private static boolean isEdge(@NotNull Set<BlockPos> set, @NotNull BlockPos pos, int ax, int ay, int az, int bx, int by, int bz) {
        boolean na = set.contains(new BlockPos(pos.getX() + ax, pos.getY() + ay, pos.getZ() + az));
        boolean nb = set.contains(new BlockPos(pos.getX() + bx, pos.getY() + by, pos.getZ() + bz));
        if (!na && !nb) return true;

        if (na && nb) return !set.contains(new BlockPos(pos.getX() + ax + bx, pos.getY() + ay + by, pos.getZ() + az + bz));
        return false;
    }

    private static void line(@NotNull Player player, @NotNull Particle.DustOptions color,
                             double x1, double y1, double z1, double x2, double y2, double z2) {
        Vector from = new Vector(x1, y1, z1);
        Vector delta = new Vector(x2 - x1, y2 - y1, z2 - z1);
        int steps = (int) Math.round(delta.length() / STEP);
        for (int i = 0; i <= steps; i++) {
            Vector point = from.clone().add(delta.clone().multiply((double) i / steps));
            player.spawnParticle(Particle.DUST, point.getX(), point.getY(), point.getZ(), 1, 0, 0, 0, 0, color);
        }
    }
}
