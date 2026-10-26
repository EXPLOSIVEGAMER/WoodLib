package at.woodexplosive.woodlib.impl.block;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.block.CustomBlockEntity;
import at.woodexplosive.woodlib.api.block.DisplayDefinition;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.LocalPos;
import at.woodexplosive.woodlib.api.block.MultiblockInstance;
import at.woodexplosive.woodlib.api.block.MultiblockShape;
import at.woodexplosive.woodlib.api.block.ToolTier;
import at.woodexplosive.woodlib.api.block.event.CustomBlockBreakEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockInteractEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockPlaceEvent;
import at.woodexplosive.woodlib.impl.block.builder.CustomBlockBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Default {@link ICustomBlock} implementation. Built via {@link CustomBlockBuilder}.
 */
public class CustomBlock implements ICustomBlock {

    protected final NamespacedKey id;
    protected final MultiblockShape shape;
    protected final List<DisplayDefinition> displays;
    protected final BlockData defaultCollision;
    protected final Map<LocalPos, BlockData> collisionOverrides;
    protected final @Nullable Integer offset;
    protected final int heightOffset;
    protected final boolean rotatable;
    protected final float hardness;
    protected final @Nullable Tag<Material> requiredToolType;
    protected final @Nullable ToolTier minimumToolTier;
    protected final @NotNull List<Tag<Material>> blockTags;

    protected final @NotNull Consumer<CustomBlockInteractEvent> onInteract;
    protected final @NotNull Consumer<CustomBlockPlaceEvent> onPlace;
    protected final @NotNull Consumer<CustomBlockBreakEvent> onBreak;
    protected final @NotNull Function<MultiblockInstance, ? extends CustomBlockEntity> entityFactory;

    /**
     * @param offset the placement offset, or {@code null} for the {@link ICustomBlock#offset() default}
     */
    public CustomBlock(@NotNull NamespacedKey id, @NotNull MultiblockShape shape, @NotNull List<DisplayDefinition> displays,
                       @NotNull BlockData defaultCollision, @NotNull Map<LocalPos, BlockData> collisionOverrides,
                       @Nullable Integer offset, int heightOffset, boolean rotatable,
                       float hardness, @Nullable Tag<Material> requiredToolType, @Nullable ToolTier minimumToolTier,
                       @NotNull List<Tag<Material>> blockTags,
                       @NotNull Consumer<CustomBlockInteractEvent> onInteract,
                       @NotNull Consumer<CustomBlockPlaceEvent> onPlace,
                       @NotNull Consumer<CustomBlockBreakEvent> onBreak,
                       @NotNull Function<MultiblockInstance, ? extends CustomBlockEntity> entityFactory) {
        this.id = id;
        this.shape = shape;
        this.displays = List.copyOf(displays);
        this.defaultCollision = defaultCollision.clone();
        this.collisionOverrides = new HashMap<>(collisionOverrides);
        this.offset = offset;
        this.heightOffset = heightOffset;
        this.rotatable = rotatable;
        this.hardness = hardness;
        this.requiredToolType = requiredToolType;
        this.minimumToolTier = minimumToolTier;
        this.blockTags = List.copyOf(blockTags);
        this.onInteract = onInteract;
        this.onPlace = onPlace;
        this.onBreak = onBreak;
        this.entityFactory = entityFactory;
    }

    @Override
    public @NotNull NamespacedKey id() {
        return id;
    }

    @Override
    public @NotNull MultiblockShape shape() {
        return shape;
    }

    @Override
    public @NotNull List<DisplayDefinition> displays() {
        return displays;
    }

    @Override
    public @NotNull BlockData collisionData(@NotNull LocalPos pos) {
        return collisionOverrides.getOrDefault(pos, defaultCollision).clone();
    }

    @Override
    public int offset() {
        return offset != null ? offset : ICustomBlock.super.offset();
    }

    @Override
    public int heightOffset() {
        return heightOffset;
    }

    @Override
    public boolean rotatable() {
        return rotatable;
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
    public @Nullable CustomBlockEntity createEntity(@NotNull MultiblockInstance instance) {
        return entityFactory.apply(instance);
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
     * @return {@code true} if {@code block} is a cell of a placed CustomBlock structure
     */
    public static boolean isCustomBlock(Block block) {
        return CustomBlockRuntime.resolve(block, WoodLib.plugin()) != null;
    }
}
