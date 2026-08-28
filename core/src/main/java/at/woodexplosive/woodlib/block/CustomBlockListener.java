package at.woodexplosive.woodlib.block;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.block.CustomBlockRegistry;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.event.CustomBlockBreakEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockInteractEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockPlaceEvent;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The single Bukkit {@link Listener} driving the whole CustomBlock system: right-click placement,
 * left-click breaking, and right-click interaction with already-placed structures.
 *
 * <p>Registered once from {@code WoodLib.rListeners()}. Runs at {@link EventPriority#HIGH} so it only
 * acts on interactions that already passed region-protection plugins (WorldGuard etc., which
 * typically cancel at LOW/NORMAL) - no separate protection integration is needed.</p>
 *
 * <p>Breaking is driven by {@code LEFT_CLICK_BLOCK}, not {@link org.bukkit.event.block.BlockBreakEvent},
 * because {@link Material#BARRIER} (the default part material) is unbreakable in vanilla survival and
 * never produces a real break event.</p>
 */
public final class CustomBlockListener implements Listener {

    /**
     * Dispatches a main-hand right-click on a block to the placement flow (if the item in hand is
     * linked to an {@link ICustomBlock}) or the interact flow (otherwise).
     * @param event the underlying Bukkit interact event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClick(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;

        Block clickedBlock = event.getClickedBlock();
        BlockFace clickedFace = event.getBlockFace();
        if (clickedBlock == null) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        NamespacedKey linkedId = ICustomBlock.linkedIdOf(itemInHand);

        if (linkedId != null) {
            handlePlace(event, player, linkedId, itemInHand, clickedBlock, clickedFace);
        } else {
            handleInteract(event, player, clickedBlock, itemInHand);
        }
    }

    /**
     * Dispatches a main-hand left-click on a known CustomBlock part block to the break flow.
     * @param event the underlying Bukkit interact event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeftClick(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        Plugin plugin = WoodLib.plugin();
        if (!CustomBlockData.hasCustomBlockData(clickedBlock, plugin)) return;

        handleBreak(event, event.getPlayer(), clickedBlock);
    }

    /**
     * Places the {@link ICustomBlock} linked to {@code itemInHand}, if all of its parts' target
     * positions are free, firing a cancellable {@link CustomBlockPlaceEvent} first.
     * @param event the underlying interact event, canceled once a linked item is recognized
     * @param player the placing player
     * @param linkedId the CustomBlock id read off {@code itemInHand}
     * @param itemInHand the linked placer item in the player's main hand
     * @param clickedBlock the block the player right-clicked
     * @param face the face of {@code clickedBlock} that was clicked
     */
    private void handlePlace(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull NamespacedKey linkedId,
                              @NotNull ItemStack itemInHand, @NotNull Block clickedBlock, @NotNull BlockFace face) {
        ICustomBlock customBlock = CustomBlockRegistry.get(linkedId);
        if (customBlock == null) return;

        GameMode gameMode = player.getGameMode();
        if (gameMode == GameMode.SPECTATOR || gameMode == GameMode.ADVENTURE) return;

        // A linked item was recognized: claim the interaction regardless of outcome so nothing else
        // (vanilla placement of the physical item, other plugins) reacts to it too.
        event.setCancelled(true);

        Block origin = clickedBlock.getRelative(face);
        int rotationSteps = customBlock.rotatable() ? CustomBlockRuntime.yawToRotationSteps(player.getYaw()) : 0;

        if (!CustomBlockRuntime.allTargetsPlaceable(customBlock, origin, rotationSteps)) return;

        CustomBlockPlaceEvent placeEvent = new CustomBlockPlaceEvent(player, customBlock, origin, clickedBlock, face, rotationSteps, itemInHand);
        customBlock.onPlace(placeEvent);
        if (!placeEvent.callEvent()) return;

        CustomBlockRuntime.placeParts(customBlock, origin, rotationSteps);

        if (gameMode != GameMode.CREATIVE) {
            itemInHand.subtract(1);
            player.getInventory().setItemInMainHand(itemInHand);
        }
    }

    /**
     * Breaks the CustomBlock structure {@code clickedBlock} belongs to on a direct left-click, unless it
     * needs timed mining ({@link ICustomBlock#hardness()} {@code > 0}) - that path is entirely driven by
     * {@link CustomBlockDigging} off the raw digging packets instead, since Bukkit never surfaces a
     * "player is still holding down the mouse" signal for an (in survival) unbreakable Barrier.
     * @param event the underlying interact event, always cancelled once a known part is clicked
     * @param player the breaking player
     * @param clickedBlock the specific part block that was left-clicked
     */
    private void handleBreak(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull Block clickedBlock) {
        Plugin plugin = WoodLib.plugin();
        CustomBlockRuntime.ResolvedPart resolved = CustomBlockRuntime.resolve(clickedBlock, plugin);

        event.setCancelled(true);

        if (resolved == null) {
            // Stale/unregistered data (e.g. the definition was removed across an update): best-effort
            // self-repair of just this one block rather than guessing at a structure we can no longer identify.
            new CustomBlockData(clickedBlock, plugin).clear();
            clickedBlock.setType(Material.AIR, false);
            return;
        }

        if (resolved.customBlock().hardness() > 0f) return; // CustomBlockDigging drives timed breaking

        GameMode gameMode = player.getGameMode();
        if (gameMode == GameMode.SPECTATOR || gameMode == GameMode.ADVENTURE) return;

        performBreak(player, resolved, clickedBlock);
    }

    /**
     * Fires a cancellable {@link CustomBlockBreakEvent}, removes the structure's parts on success, and
     * drops loot unless the player's held tool didn't qualify as "correct" for {@code resolved}'s
     * CustomBlock. Shared by the instant-break path ({@link #handleBreak}) and the timed-mining path
     * ({@link CustomBlockDigging}), so both end up with identical break semantics.
     * @param player the breaking player
     * @param resolved the resolved CustomBlock structure
     * @param clickedBlock the specific part block that triggered the break
     */
    static void performBreak(@NotNull Player player, @NotNull CustomBlockRuntime.ResolvedPart resolved, @NotNull Block clickedBlock) {
        CustomBlockBreakEvent breakEvent = new CustomBlockBreakEvent(player, resolved.customBlock(), resolved.originBlock(),
                clickedBlock, resolved.partIndex(), resolved.rotationSteps());

        resolved.customBlock().onBreak(breakEvent);
        if (!breakEvent.callEvent()) return;

        CustomBlockRuntime.removeParts(resolved.customBlock(), resolved.originBlock(), resolved.rotationSteps());

        // Computed before damaging the held item: a tool that breaks (hits 0 durability) on this exact
        // swing would otherwise leave the hand empty by the time this is checked, wrongly reading as "no
        // tool" and suppressing drops it should have earned.
        boolean correctTool = CustomBlockMining.isCorrectTool(resolved.customBlock(), player.getInventory().getItemInMainHand());

        // Vanilla damages a damageable held item on every block break, correct tool or not - never in
        // Creative. damageItemStack already applies Unbreaking's chance and handles the item breaking.
        if (player.getGameMode() != GameMode.CREATIVE) {
            player.damageItemStack(EquipmentSlot.HAND, 1);
        }

        if (breakEvent.isDropping() && correctTool) {
            dropLoot(resolved.customBlock().id(), player, clickedBlock);
        }
    }

    /**
     * Drops the loot for a broken CustomBlock at {@code clickedBlock}'s location: rolls the registered
     * {@link LootTable} if one was set via {@link CustomBlockRegistry#registerLootTable(NamespacedKey, NamespacedKey)},
     * otherwise falls back to a single copy of the registered {@link CustomBlockRegistry#dropItem(NamespacedKey) drop item}.
     * @param blockId the broken CustomBlock's id
     * @param player the breaking player (used as the loot table's killer context)
     * @param clickedBlock the specific part block that was broken, used as the drop location
     */
    private static void dropLoot(@NotNull NamespacedKey blockId, @NotNull Player player, @NotNull Block clickedBlock) {
        LootTable lootTable = CustomBlockRegistry.lootTable(blockId);

        if (lootTable != null) {
            LootContext context = new LootContext.Builder(clickedBlock.getLocation())
                    .killer(player)
                    .build();
            for (ItemStack drop : lootTable.populateLoot(ThreadLocalRandom.current(), context)) {
                clickedBlock.getWorld().dropItemNaturally(clickedBlock.getLocation().add(0.5, 0.5, 0.5), drop);
            }
            return;
        }

        ItemStack dropItem = CustomBlockRegistry.dropItem(blockId);
        if (dropItem != null) {
            clickedBlock.getWorld().dropItemNaturally(clickedBlock.getLocation().add(0.5, 0.5, 0.5), dropItem.asOne());
        }
    }

    /**
     * Fires a {@link CustomBlockInteractEvent} for a right-click on an already-placed CustomBlock while
     * not holding a linked item. Purely observational - no default action beyond firing the event.
     * @param event the underlying interact event, cancelled once a known part is clicked
     * @param player the interacting player
     * @param clickedBlock the specific part block that was right-clicked
     * @param itemInHand the item in the player's main hand, possibly empty
     */
    private void handleInteract(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull Block clickedBlock,
                                 @NotNull ItemStack itemInHand) {
        Plugin plugin = WoodLib.plugin();
        if (!CustomBlockData.hasCustomBlockData(clickedBlock, plugin)) return;

        CustomBlockRuntime.ResolvedPart resolved = CustomBlockRuntime.resolve(clickedBlock, plugin);
        if (resolved == null) return; // stale data - interact is read-only, no destructive cleanup here

        event.setCancelled(true);

        CustomBlockInteractEvent interactEvent = new CustomBlockInteractEvent(player, resolved.customBlock(), resolved.originBlock(),
                clickedBlock, resolved.partIndex(), resolved.rotationSteps(), EquipmentSlot.HAND, itemInHand);
        resolved.customBlock().onInteract(interactEvent);
        interactEvent.callEvent();
    }
}
