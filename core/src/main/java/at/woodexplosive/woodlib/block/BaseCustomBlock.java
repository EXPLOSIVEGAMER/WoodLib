package at.woodexplosive.woodlib.block;

import at.woodexplosive.woodlib.api.block.CustomBlockPart;
import at.woodexplosive.woodlib.api.block.ToolTier;
import at.woodexplosive.woodlib.api.block.event.CustomBlockBreakEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockInteractEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockPlaceEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public abstract class BaseCustomBlock extends CustomBlock {
    public BaseCustomBlock(@NotNull NamespacedKey id, @NotNull List<CustomBlockPart> parts, boolean rotatable, float hardness, @Nullable Tag<Material> requiredToolType, @Nullable ToolTier minimumToolTier, @NotNull List<Tag<Material>> blockTags, @NotNull Consumer<CustomBlockInteractEvent> onInteract, @NotNull Consumer<CustomBlockPlaceEvent> onPlace, @NotNull Consumer<CustomBlockBreakEvent> onBreak) {
        super(id, parts, rotatable, hardness, requiredToolType, minimumToolTier, blockTags, onInteract, onPlace, onBreak);
    }

    protected BaseCustomBlock() {}

    public abstract @NotNull NamespacedKey id();

    public abstract @NotNull List<CustomBlockPart> parts();

    public abstract float hardness();
}
