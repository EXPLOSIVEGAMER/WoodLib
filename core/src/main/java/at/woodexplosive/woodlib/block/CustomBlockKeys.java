package at.woodexplosive.woodlib.block;

import at.woodexplosive.woodlib.WoodLib;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * {@link NamespacedKey} factories for the tags stamped on each placed {@link at.woodexplosive.woodlib.api.block.CustomBlockPart}'s
 * {@code CustomBlockData} (and, where noted, on spawned display entities). The "which CustomBlock"
 * tag itself is {@link at.woodexplosive.woodlib.api.block.ICustomBlock#idKey()}, reused everywhere an
 * id needs to be stamped.
 */
public final class CustomBlockKeys {

    private CustomBlockKeys() {}

    /**
     * Origin coordinates of the structure this part belongs to, as an {@code int[3]} {@code [x, y, z]}
     * in the same world as the tagged block/entity. Also stamped on display entities.
     */
    static @NotNull NamespacedKey origin() {
        return new NamespacedKey(WoodLib.plugin(), "custom_block_origin");
    }

    /** The structure's rotation in 90° steps (0-3), as it was placed. */
    static @NotNull NamespacedKey rotation() {
        return new NamespacedKey(WoodLib.plugin(), "custom_block_rotation");
    }

    /** The index of this part into its {@link at.woodexplosive.woodlib.api.block.ICustomBlock#parts()}. */
    static @NotNull NamespacedKey partIndex() {
        return new NamespacedKey(WoodLib.plugin(), "custom_block_part_index");
    }
}
