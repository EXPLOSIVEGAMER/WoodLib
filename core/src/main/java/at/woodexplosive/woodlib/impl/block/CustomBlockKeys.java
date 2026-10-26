package at.woodexplosive.woodlib.impl.block;

import at.woodexplosive.woodlib.WoodLib;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * {@link NamespacedKey} factories for the tags stamped on each placed CustomBlock cell's
 * {@code CustomBlockData} (and, where noted, on spawned display entities). The "which CustomBlock"
 * tag itself is {@link at.woodexplosive.woodlib.api.block.ICustomBlock#idKey()}, reused everywhere an
 * id needs to be stamped.
 */
public final class CustomBlockKeys {

    private CustomBlockKeys() {}

    /**
     * Core coordinates of the structure a dummy belongs to, as an {@code int[3]} {@code [x, y, z]} in the
     * same world as the tagged block/entity. Stamped on dummies, ports and display entities - the
     * counterpart of HBM's dummy metadata pointing towards the core.
     */
    static @NotNull NamespacedKey core() {
        return new NamespacedKey(WoodLib.plugin(), "custom_block_core");
    }

    /** The structure's facing ({@link org.bukkit.block.BlockFace} name), stamped on the core only. */
    static @NotNull NamespacedKey facing() {
        return new NamespacedKey(WoodLib.plugin(), "custom_block_facing");
    }

    /** The cell's {@link CustomBlockRuntime.Role} ordinal, as a byte. */
    static @NotNull NamespacedKey role() {
        return new NamespacedKey(WoodLib.plugin(), "custom_block_role");
    }
}
