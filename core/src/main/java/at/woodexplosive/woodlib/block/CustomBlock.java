package at.woodexplosive.woodlib.block;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.block.CustomBlockPart;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.ToolTier;
import at.woodexplosive.woodlib.api.block.event.CustomBlockBreakEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockInteractEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockPlaceEvent;
import at.woodexplosive.woodlib.block.builder.CustomBlockBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Default {@link ICustomBlock} implementation. Built via {@link CustomBlockBuilder}.
 */
public class CustomBlock implements ICustomBlock {

    protected NamespacedKey id;
    protected List<CustomBlockPart> parts;
    protected boolean rotatable;
    protected float hardness;
    protected @Nullable Tag<Material> requiredToolType;
    protected @Nullable ToolTier minimumToolTier;
    protected @NotNull List<Tag<Material>> blockTags = List.of();

    protected @NotNull Consumer<CustomBlockInteractEvent> onInteract = event -> {};
    protected @NotNull Consumer<CustomBlockPlaceEvent> onPlace = event -> {};
    protected @NotNull Consumer<CustomBlockBreakEvent> onBreak = event -> {};

    public CustomBlock(@NotNull NamespacedKey id, @NotNull List<CustomBlockPart> parts, boolean rotatable,
                       float hardness, @Nullable Tag<Material> requiredToolType, @Nullable ToolTier minimumToolTier,
                       @NotNull List<Tag<Material>> blockTags,
                       @NotNull Consumer<CustomBlockInteractEvent> onInteract,
                       @NotNull Consumer<CustomBlockPlaceEvent> onPlace,
                       @NotNull Consumer<CustomBlockBreakEvent> onBreak) {
        this.id = id;
        this.parts = List.copyOf(parts);
        this.rotatable = rotatable;
        this.hardness = hardness;
        this.requiredToolType = requiredToolType;
        this.minimumToolTier = minimumToolTier;
        this.blockTags = List.copyOf(blockTags);
        this.onInteract = onInteract;
        this.onPlace = onPlace;
        this.onBreak = onBreak;
    }

    protected CustomBlock() {}

    @Override
    public @NotNull NamespacedKey id() {
        return id;
    }

    @Override
    public @NotNull List<CustomBlockPart> parts() {
        return parts;
    }

    @Override
    public float hardness() {
        return hardness;
    }

    @Override
    public @Nullable Tag<Material> requiredToolType() {
        return requiredToolType;
    }

    @Override
    public @Nullable ToolTier minimumToolTier() {
        return minimumToolTier;
    }

    @Override
    public @NotNull List<Tag<Material>> blockTags() {
        return blockTags;
    }

    @Override
    public void onInteract(@NotNull CustomBlockInteractEvent event) {
        onInteract.accept(event);
    }

    @Override
    public void onBreak(@NotNull CustomBlockBreakEvent event) {
        onBreak.accept(event);
    }

    @Override
    public void onPlace(@NotNull CustomBlockPlaceEvent event) {
        onPlace.accept(event);
    }

    /**
     * @param block the world block to check
     * @return {@code true} if {@code block} is a part of a placed CustomBlock structure
     */
    public static boolean isCustomBlock(Block block) {
        return CustomBlockRuntime.resolve(block, WoodLib.plugin()) != null;
    }
}
