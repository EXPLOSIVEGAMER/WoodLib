package at.woodexplosive.woodlib.api.block;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.block.event.CustomBlockBreakEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockInteractEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockPlaceEvent;
import at.woodexplosive.woodlib.block.builder.CustomBlockBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * A registered "fake" custom block definition: one or more {@link CustomBlockPart}s, each a real
 * {@link org.bukkit.Material#BARRIER Barrier} (or other material) for collision plus one or more
 * {@link DisplayDefinition BlockDisplays} for the visual.
 *
 * <p>Instances are built with {@link CustomBlockBuilder} and made
 * usable by registering them, together with their placer/drop item, in
 * {@link CustomBlockRegistry#register(ICustomBlock, ItemStack)}. An item is linked to a CustomBlock
 * via {@link at.woodexplosive.woodlib.api.item.AbstractItemBuilder#linkCustomBlock(ICustomBlock)}.</p>
 */
public interface ICustomBlock {

    /**
     * This CustomBlock's unique registry id (e.g. {@code "myplugin:custom_furnace"}).
     * @return the id
     */
    @Contract(pure = true)
    @NotNull NamespacedKey id();

    /**
     * The parts making up this CustomBlock's structure, relative to its placement origin. Never
     * empty; a single-block CustomBlock has exactly one part at offset {@code (0,0,0)}.
     * @return the immutable list of parts
     */
    @Contract(pure = true)
    @NotNull List<CustomBlockPart> parts();

    /**
     * Called when a player interacts with a placed instance of this CustomBlock. No-op by default.
     * @param event the interact event
     */
    default void onInteract(CustomBlockInteractEvent event) {}

    /**
     * Called when a placed instance of this CustomBlock is broken. No-op by default.
     * @param event the break event
     */
    default void onBreak(CustomBlockBreakEvent event) {}

    /**
     * Called when an instance of this CustomBlock is placed. No-op by default.
     * @param event the place event
     */
    default void onPlace(CustomBlockPlaceEvent event) {}

    /**
     * Whether this CustomBlock is placed with a 4-way cardinal rotation snapped from the placing
     * player's yaw. If {@code false} (the default), it is always placed in its authored orientation.
     * @return {@code true} if placement rotates the structure
     */
    @Contract(pure = true)
    default boolean rotatable() {
        return false;
    }

    /**
     * This CustomBlock's mining hardness, analogous to a vanilla block's hardness stat: higher values
     * take longer to mine, taking the mining player's held tool, enchantments and status effects into
     * account exactly like a vanilla block would. {@code <= 0} (the default) means instant break on
     * left-click, matching every CustomBlock defined before this existed.
     * @return the hardness, or {@code <= 0} for instant break
     */
    @Contract(pure = true)
    default float hardness() {
        return 0f;
    }

    /**
     * The vanilla item {@link Tag} a held item must belong to for it to count as the "correct tool" (e.g.
     * {@link Tag#ITEMS_PICKAXES}). Only consulted when {@link #hardness()} is {@code > 0}. {@code null}
     * (the default) means hand-mineable - any item (or no item) always counts as correct.
     * @return the required tool type tag, or {@code null} if none is required
     */
    @Contract(pure = true)
    default @Nullable Tag<Material> requiredToolType() {
        return null;
    }

    /**
     * The minimum {@link ToolTier} a held tool must be to count as "correct" once
     * {@link #requiredToolType()} already matched. {@code null} (the default) means no minimum - any tier
     * of the required type works. Only consulted when {@link #requiredToolType()} is non-null.
     * @return the minimum tool tier, or {@code null} for no minimum
     */
    @Contract(pure = true)
    default @Nullable ToolTier minimumToolTier() {
        return null;
    }

    /**
     * Vanilla block {@link Tag}s this CustomBlock is declared to belong to (e.g. {@link Tag#CROPS}) -
     * pure metadata for other code to query via {@link #hasBlockTag(Tag)}. Attaching a tag here does
     * <b>not</b> confer any of its vanilla behavior: every part is a real
     * {@link org.bukkit.Material#BARRIER Barrier} (or another collision-only material), so growth ticks,
     * bonemeal, trampling etc. never run on it - those are wired to the actual placed block's own class,
     * not to tag membership. {@code List.of()} (the default) means no tags declared.
     * @return the declared block tags
     */
    @Contract(pure = true)
    default @NotNull List<Tag<Material>> blockTags() {
        return List.of();
    }

    /**
     * @param tag the block tag to check
     * @return {@code true} if {@code tag} is in {@link #blockTags()}
     */
    @Contract(pure = true)
    default boolean hasBlockTag(@NotNull Tag<Material> tag) {
        return blockTags().contains(tag);
    }

    /**
     * The {@link NamespacedKey} a CustomBlock's {@link #id()} is stamped under: on a linked placer
     * item's {@link ItemMeta} persistent data, on each placed part's
     * {@code com.jeff_media.customblockdata.CustomBlockData}, and on each spawned display entity's own
     * persistent data container.
     * @return the id {@link NamespacedKey}
     */
    @Contract(pure = true)
    static @NotNull NamespacedKey idKey() {
        return new NamespacedKey(WoodLib.plugin(), "custom_block_id");
    }

    /**
     * Reads the {@link ICustomBlock} id stamped on the given item by
     * {@link at.woodexplosive.woodlib.api.item.AbstractItemBuilder#linkCustomBlock(ICustomBlock)}, if any.
     * @param stack the item to inspect
     * @return the linked CustomBlock id, or {@code null} if the item isn't linked to one
     */
    @Contract(pure = true)
    static @Nullable NamespacedKey linkedIdOf(@NotNull ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;
        String sID = meta.getPersistentDataContainer().get(idKey(), PersistentDataType.STRING);
        if (sID == null) return null;
        return NamespacedKey.fromString(sID);
    }

    /**
     * @return a new identity {@link Transformation} (no translation, rotation or scale change)
     */
    @Contract(value = "-> new", pure = true)
    static @NotNull Transformation emptyTransformation() {
        return new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf());
    }
}
