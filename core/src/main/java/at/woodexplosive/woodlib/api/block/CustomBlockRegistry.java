package at.woodexplosive.woodlib.api.block;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry of every known {@link ICustomBlock}, keyed by {@link ICustomBlock#id()}, together with the
 * item dropped when a placed instance is broken and, optionally, a datapack {@link LootTable} to roll
 * instead of that fixed item.
 *
 * <p>Registration is plain in-memory bookkeeping with no dependency on {@link at.woodexplosive.woodlib.WoodLib}
 * being initialized yet, so consumers may register their CustomBlocks as early as {@code onLoad()}.
 * All identity of already-placed structures lives in world/entity persistent data, not in this
 * registry, so {@link #clear()} (called by {@code WoodLib.disable()}) never affects placed blocks -
 * it only drops the in-memory definitions until they're registered again.</p>
 */
public final class CustomBlockRegistry {

    private static final Map<NamespacedKey, ICustomBlock> BLOCKS = new HashMap<>();
    private static final Map<NamespacedKey, ItemStack> DROP_ITEMS = new HashMap<>();
    private static final Map<NamespacedKey, NamespacedKey> LOOT_TABLES = new HashMap<>();

    private CustomBlockRegistry() {}

    /**
     * Registers a {@link ICustomBlock} together with the item dropped when it's broken.
     * @param block the CustomBlock definition
     * @param dropItem the item given back to players (given on placement via
     *                 {@link at.woodexplosive.woodlib.api.item.AbstractItemBuilder#linkCustomBlock(ICustomBlock)},
     *                 and dropped when the placed structure is broken, unless a {@link #registerLootTable(NamespacedKey, NamespacedKey) loot table}
     *                 is also registered for this id)
     */
    public static void register(@NotNull ICustomBlock block, @NotNull ItemStack dropItem) {
        BLOCKS.put(block.id(), block);
        DROP_ITEMS.put(block.id(), dropItem);
    }

    /**
     * Registers a datapack {@link LootTable} to roll when this CustomBlock is broken, instead of
     * dropping a fixed copy of its registered {@link #dropItem(NamespacedKey) drop item}.
     *
     * <p>The table itself must already be loaded by the server - either a vanilla table
     * ({@link org.bukkit.loot.LootTables}) or a custom one shipped in a datapack under
     * {@code data/<namespace>/loot_table/<path>.json}. This only remembers which key to resolve via
     * {@link Bukkit#getLootTable(NamespacedKey)} at break time (see {@link #lootTable(NamespacedKey)}),
     * so a datapack reload picks up table changes without re-registering here.</p>
     *
     * @param blockId the CustomBlock id (should already be {@link #register(ICustomBlock, ItemStack) registered})
     * @param lootTableKey the loot table's own key (e.g. {@code new NamespacedKey(plugin, "custom_block/my_block")})
     */
    public static void registerLootTable(@NotNull NamespacedKey blockId, @NotNull NamespacedKey lootTableKey) {
        LOOT_TABLES.put(blockId, lootTableKey);
    }

    /**
     * Unregisters a CustomBlock (and any loot table registered for it). Does not affect already-placed
     * structures.
     * @param id the CustomBlock id
     */
    public static void unregister(@NotNull NamespacedKey id) {
        BLOCKS.remove(id);
        DROP_ITEMS.remove(id);
        LOOT_TABLES.remove(id);
    }

    /**
     * Looks up a registered CustomBlock by id.
     * @param id the CustomBlock id
     * @return the registered {@link ICustomBlock}, or {@code null} if none is registered under that id
     */
    @Contract(pure = true)
    public static @Nullable ICustomBlock get(@NotNull NamespacedKey id) {
        return BLOCKS.get(id);
    }

    /**
     * Looks up the drop item registered for a CustomBlock id.
     * @param id the CustomBlock id
     * @return a copy of the registered drop {@link ItemStack}, or {@code null} if none is registered
     */
    @Contract(pure = true)
    public static @Nullable ItemStack dropItem(@NotNull NamespacedKey id) {
        ItemStack item = DROP_ITEMS.get(id);
        return item == null ? null : item.clone();
    }

    /**
     * Resolves the {@link LootTable} registered for a CustomBlock id, if any, freshly via
     * {@link Bukkit#getLootTable(NamespacedKey)} on every call (so datapack reloads are picked up
     * automatically).
     * @param id the CustomBlock id
     * @return the loot table to roll on break, or {@code null} if none is registered, or the
     *         registered key no longer resolves to a loaded table
     */
    @Contract(pure = true)
    public static @Nullable LootTable lootTable(@NotNull NamespacedKey id) {
        NamespacedKey tableKey = LOOT_TABLES.get(id);
        return tableKey == null ? null : Bukkit.getLootTable(tableKey);
    }

    /**
     * @param id the CustomBlock id
     * @return {@code true} if a CustomBlock is registered under that id
     */
    @Contract(pure = true)
    public static boolean isRegistered(@NotNull NamespacedKey id) {
        return BLOCKS.containsKey(id);
    }

    /**
     * @return an unmodifiable view of all currently registered CustomBlocks
     */
    @Contract(pure = true)
    public static @NotNull Collection<ICustomBlock> all() {
        return Collections.unmodifiableCollection(BLOCKS.values());
    }

    /**
     * Clears all registrations. Called by {@code WoodLib.disable()} for hygiene; does not touch any
     * already-placed structure in the world.
     */
    public static void clear() {
        BLOCKS.clear();
        DROP_ITEMS.clear();
        LOOT_TABLES.clear();
    }
}
