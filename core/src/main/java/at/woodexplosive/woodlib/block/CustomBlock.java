package at.woodexplosive.woodlib.block;

import at.woodexplosive.woodlib.api.block.CustomBlockPart;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Default {@link ICustomBlock} implementation. Built via {@link CustomBlockBuilder}.
 */
public final class CustomBlock implements ICustomBlock {

    private final NamespacedKey id;
    private final List<CustomBlockPart> parts;
    private final boolean rotatable;

    CustomBlock(@NotNull NamespacedKey id, @NotNull List<CustomBlockPart> parts, boolean rotatable) {
        this.id = id;
        this.parts = List.copyOf(parts);
        this.rotatable = rotatable;
    }

    @Override
    public @NotNull NamespacedKey id() {
        return id;
    }

    @Override
    public @NotNull List<CustomBlockPart> parts() {
        return parts;
    }

    @Override
    public boolean rotatable() {
        return rotatable;
    }
}
