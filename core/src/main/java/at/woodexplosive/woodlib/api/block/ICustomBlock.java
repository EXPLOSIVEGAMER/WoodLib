package at.woodexplosive.woodlib.api.block;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.block.event.CustomBlockBreakEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockInteractEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockPlaceEvent;
import at.woodexplosive.woodlib.block.builder.CustomBlockBuilder;
import org.bukkit.NamespacedKey;
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

    default void onInteract(CustomBlockInteractEvent event) {}
    default void onBreak(CustomBlockBreakEvent event) {}
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

    @Contract(value = "-> new", pure = true)
    static @NotNull Transformation emptyTransformation() {
        return new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf());
    }
}
