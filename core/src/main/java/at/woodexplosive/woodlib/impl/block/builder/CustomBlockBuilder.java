package at.woodexplosive.woodlib.impl.block.builder;

import at.woodexplosive.woodlib.api.block.CustomBlockEntity;
import at.woodexplosive.woodlib.api.block.Dimensions;
import at.woodexplosive.woodlib.api.block.DisplayDefinition;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.LocalDirection;
import at.woodexplosive.woodlib.api.block.LocalPos;
import at.woodexplosive.woodlib.api.block.MultiblockInstance;
import at.woodexplosive.woodlib.api.block.MultiblockShape;
import at.woodexplosive.woodlib.api.block.ToolTier;
import at.woodexplosive.woodlib.api.block.builder.ICustomBlockBuilder;
import at.woodexplosive.woodlib.api.block.event.CustomBlockBreakEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockInteractEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockPlaceEvent;
import at.woodexplosive.woodlib.impl.block.CustomBlock;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Fluent builder for {@link ICustomBlock} definitions.
 *
 * <p>Without any {@link #box(Dimensions) box} the CustomBlock is a single block. A multiblock is
 * described HBM-style by its extents around the core, e.g. a 3x3x3 machine standing on its core row:
 * {@code .dimensions(2, 0, 1, 1, 1, 1)}.</p>
 */
public final class CustomBlockBuilder implements ICustomBlockBuilder<CustomBlockBuilder> {

    private final NamespacedKey id;
    private final MultiblockShape.Builder shape = MultiblockShape.builder();
    private final List<DisplayDefinition> displays = new ArrayList<>();
    private BlockData defaultCollision = Material.BARRIER.createBlockData();
    private final Map<LocalPos, BlockData> collisionOverrides = new HashMap<>();
    private Integer offset;
    private int heightOffset = 0;
    private boolean rotatable = false;
    private float hardness = 0f;
    private Tag<Material> requiredToolType;
    private ToolTier minimumToolTier;
    private List<Tag<Material>> blockTags = List.of();

    private Consumer<CustomBlockInteractEvent> onInteract = event -> {};
    private Consumer<CustomBlockPlaceEvent> onPlace = event -> {};
    private Consumer<CustomBlockBreakEvent> onBreak = event -> {};
    private Function<MultiblockInstance, ? extends CustomBlockEntity> entityFactory = instance -> null;

    private CustomBlockBuilder(@NotNull NamespacedKey id) {
        this.id = id;
    }

    /**
     * Starts a new builder for a CustomBlock with the given unique registry id.
     * @param id the CustomBlock id (e.g. {@code "myplugin:custom_furnace"})
     * @return a new {@link CustomBlockBuilder}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull CustomBlockBuilder of(@NotNull NamespacedKey id) {
        return new CustomBlockBuilder(id);
    }

    /**
     * Shorthand for {@link #box(Dimensions)} with HBM-style extents around the core.
     * @param up cells above the core
     * @param down cells below the core
     * @param forward cells in front of the core
     * @param backward cells behind the core
     * @param left cells to the structure's left
     * @param right cells to the structure's right
     * @return this builder for chaining
     */
    @Contract(value = "_, _, _, _, _, _ -> this")
    public @NotNull CustomBlockBuilder dimensions(int up, int down, int forward, int backward, int left, int right) {
        return box(new Dimensions(up, down, forward, backward, left, right));
    }

    @Contract(value = "_ -> this")
    @Override
    public @NotNull CustomBlockBuilder box(@NotNull Dimensions dimensions) {
        shape.box(dimensions);
        return this;
    }

    @Contract(value = "_, _ -> this")
    @Override
    public @NotNull CustomBlockBuilder box(@NotNull Dimensions dimensions, @NotNull LocalPos offset) {
        shape.box(dimensions, offset);
        return this;
    }

    @Contract(value = "_, _ -> this")
    @Override
    public @NotNull CustomBlockBuilder port(@NotNull LocalPos pos, @NotNull LocalDirection direction) {
        shape.port(pos, direction);
        return this;
    }

    @Contract(value = "_ -> this")
    @Override
    public @NotNull CustomBlockBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }

    @Contract(value = "_ -> this")
    @Override
    public @NotNull CustomBlockBuilder heightOffset(int heightOffset) {
        this.heightOffset = heightOffset;
        return this;
    }

    @Contract(value = "_ -> this")
    @Override
    public @NotNull CustomBlockBuilder display(@NotNull DisplayDefinition display) {
        this.displays.add(display);
        return this;
    }

    /**
     * Adds several displays rendered at the core.
     * @param displays the displays
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    public @NotNull CustomBlockBuilder displays(@NotNull List<DisplayDefinition> displays) {
        this.displays.addAll(displays);
        return this;
    }

    /**
     * Convenience for the common single-block case: adds the given displays, without adding any box.
     * @param displays the {@link DisplayDefinition}s rendered at the block
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    public @NotNull CustomBlockBuilder single(@NotNull List<DisplayDefinition> displays) {
        return displays(displays);
    }

    @Contract(value = "_ -> this")
    @Override
    public @NotNull CustomBlockBuilder collision(@NotNull BlockData data) {
        this.defaultCollision = data.clone();
        return this;
    }

    /**
     * Sets the collision material used for every cell without an explicit override.
     * @param material the collision material
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    public @NotNull CustomBlockBuilder collision(@NotNull Material material) {
        return collision(material.createBlockData());
    }

    @Contract(value = "_, _ -> this")
    @Override
    public @NotNull CustomBlockBuilder collision(@NotNull LocalPos pos, @NotNull BlockData data) {
        this.collisionOverrides.put(pos, data.clone());
        return this;
    }

    @Contract(value = "_ -> this")
    @Override
    public @NotNull CustomBlockBuilder rotatable(boolean rotatable) {
        this.rotatable = rotatable;
        return this;
    }

    @Contract(value = "_ -> this")
    @Override
    public @NotNull CustomBlockBuilder hardness(float hardness) {
        this.hardness = hardness;
        return this;
    }

    @Contract(value = "_ -> this")
    @Override
    public @NotNull CustomBlockBuilder requiredToolType(@Nullable Tag<Material> toolType) {
        this.requiredToolType = toolType;
        return this;
    }

    @Contract(value = "_ -> this")
    @Override
    public @NotNull CustomBlockBuilder minimumToolTier(@Nullable ToolTier tier) {
        this.minimumToolTier = tier;
        return this;
    }

    @Contract(value = "_ -> this")
    @Override
    public @NotNull CustomBlockBuilder blockTags(@NotNull List<Tag<Material>> tags) {
        this.blockTags = List.copyOf(tags);
        return this;
    }

    @Override
    public @NotNull CustomBlockBuilder setOnBlockInteract(@NotNull Consumer<CustomBlockInteractEvent> event) {
        this.onInteract = event;
        return this;
    }

    @Override
    public @NotNull CustomBlockBuilder setOnBlockBreakEvent(@NotNull Consumer<CustomBlockBreakEvent> event) {
        this.onBreak = event;
        return this;
    }

    @Override
    public @NotNull CustomBlockBuilder setOnBlockPlaceEvent(@NotNull Consumer<CustomBlockPlaceEvent> event) {
        this.onPlace = event;
        return this;
    }

    @Override
    public @NotNull CustomBlockBuilder entity(@NotNull Function<MultiblockInstance, ? extends CustomBlockEntity> factory) {
        this.entityFactory = factory;
        return this;
    }

    @Contract(value = "-> new")
    @Override
    public @NotNull ICustomBlock build() {
        MultiblockShape built = shape.build();
        for (LocalPos pos : collisionOverrides.keySet()) {
            if (!built.cells().contains(pos)) {
                throw new IllegalArgumentException("CustomBlock '" + id + "' has a collision override outside its shape: " + pos);
            }
        }
        return new CustomBlock(id, built, displays, defaultCollision, collisionOverrides, offset, heightOffset, rotatable,
                hardness, requiredToolType, minimumToolTier, blockTags, onInteract, onPlace, onBreak, entityFactory);
    }
}
