package at.woodexplosive.woodlib.api.block.builder;

import at.woodexplosive.woodlib.api.block.CustomBlockEntity;
import at.woodexplosive.woodlib.api.block.Dimensions;
import at.woodexplosive.woodlib.api.block.DisplayDefinition;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.LocalDirection;
import at.woodexplosive.woodlib.api.block.LocalPos;
import at.woodexplosive.woodlib.api.block.MultiblockInstance;
import at.woodexplosive.woodlib.api.block.ToolTier;
import at.woodexplosive.woodlib.api.block.event.CustomBlockBreakEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockInteractEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockPlaceEvent;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Fluent builder for assembling an HBM-style multiblock {@link ICustomBlock}: its shape (boxes around
 * the core plus ports), placement offsets, core-anchored displays, collision blocks, and its mining and
 * interaction behavior.
 * @param <T> the concrete builder type, for covariant method chaining
 */
public interface ICustomBlockBuilder<T extends ICustomBlockBuilder<T>> {

    /**
     * Adds a box around the core (HBM's {@code getDimensions()}). Without any box, the CustomBlock is a
     * single block.
     * @param dimensions the box's extents
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    @NotNull T box(@NotNull Dimensions dimensions);

    /**
     * Adds a box around {@code offset} instead of the core, for compound shapes (HBM's
     * {@code getAllDimensions()}).
     * @param dimensions the box's extents
     * @param offset the box's center, relative to the core
     * @return this builder for chaining
     */
    @Contract(value = "_, _ -> this")
    @NotNull T box(@NotNull Dimensions dimensions, @NotNull LocalPos offset);

    /**
     * Adds a port (HBM's "extra" dummy) at a cell of the shape.
     * @param pos the port's cell
     * @param direction the side the port connects through
     * @return this builder for chaining
     */
    @Contract(value = "_, _ -> this")
    @NotNull T port(@NotNull LocalPos pos, @NotNull LocalDirection direction);

    /**
     * Sets how far the core is pushed away from the placing player on floor/ceiling placement.
     * Defaults to the shape's forward extent.
     * @param offset the offset in blocks
     * @return this builder for chaining
     * @see ICustomBlock#offset()
     */
    @Contract(value = "_ -> this")
    @NotNull T offset(int offset);

    /**
     * Sets the extra vertical shift of the core on placement. Defaults to {@code 0}.
     * @param heightOffset the height offset in blocks
     * @return this builder for chaining
     * @see ICustomBlock#heightOffset()
     */
    @Contract(value = "_ -> this")
    @NotNull T heightOffset(int heightOffset);

    /**
     * Adds a display rendered at the core.
     * @param display the display
     * @return this builder for chaining
     * @see ICustomBlock#displays()
     */
    @Contract(value = "_ -> this")
    @NotNull T display(@NotNull DisplayDefinition display);

    /**
     * Sets the collision block used for every cell without an explicit override. Defaults to
     * {@link Material#BARRIER}.
     * @param data the collision block data, authored facing north
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    @NotNull T collision(@NotNull BlockData data);

    /**
     * Sets the collision block for one cell.
     * @param pos the cell
     * @param data the collision block data, authored facing north
     * @return this builder for chaining
     */
    @Contract(value = "_, _ -> this")
    @NotNull T collision(@NotNull LocalPos pos, @NotNull BlockData data);

    /**
     * Sets whether this CustomBlock is placed facing the placing player. Defaults to {@code false}.
     * @param rotatable {@code true} to enable placement rotation
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    @NotNull T rotatable(boolean rotatable);

    /**
     * Sets this CustomBlock's mining hardness. {@code <= 0} (the default) means instant break.
     * @param hardness the hardness, or {@code <= 0} for instant break
     * @return this builder for chaining
     * @see ICustomBlock#hardness()
     */
    @Contract(value = "_ -> this")
    @NotNull T hardness(float hardness);

    /**
     * Sets the vanilla item {@link Tag} a held item must belong to for it to count as the "correct tool".
     * @param toolType the required tool type tag, or {@code null} for hand-mineable
     * @return this builder for chaining
     * @see ICustomBlock#requiredToolType()
     */
    @Contract(value = "_ -> this")
    @NotNull T requiredToolType(@Nullable Tag<Material> toolType);

    /**
     * Sets the minimum {@link ToolTier} a held tool must be, once {@link #requiredToolType(Tag)} already
     * matched.
     * @param tier the minimum tool tier, or {@code null} for no minimum
     * @return this builder for chaining
     * @see ICustomBlock#minimumToolTier()
     */
    @Contract(value = "_ -> this")
    @NotNull T minimumToolTier(@Nullable ToolTier tier);

    /**
     * Sets the vanilla block {@link Tag}s this CustomBlock is declared to belong to. Pure metadata - see
     * {@link ICustomBlock#blockTags()}.
     * @param tags the block tags to declare
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    @NotNull T blockTags(@NotNull List<Tag<Material>> tags);

    /**
     * Sets the callback invoked on {@link ICustomBlock#onInteract(CustomBlockInteractEvent)}.
     * @param event the interact callback
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    @NotNull T setOnBlockInteract(@NotNull Consumer<CustomBlockInteractEvent> event);

    /**
     * Sets the callback invoked on {@link ICustomBlock#onBreak(CustomBlockBreakEvent)}.
     * @param event the break callback
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    @NotNull T setOnBlockBreakEvent(@NotNull Consumer<CustomBlockBreakEvent> event);

    /**
     * Sets the callback invoked on {@link ICustomBlock#onPlace(CustomBlockPlaceEvent)}.
     * @param event the place callback
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    @NotNull T setOnBlockPlaceEvent(@NotNull Consumer<CustomBlockPlaceEvent> event);

    /**
     * Sets the factory for the {@link CustomBlockEntity} of every placed instance
     * (see {@link ICustomBlock#createEntity(MultiblockInstance)}).
     * @param factory creates the entity for a placed structure
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    @NotNull T entity(@NotNull Function<MultiblockInstance, ? extends CustomBlockEntity> factory);

    /**
     * Builds the configured {@link ICustomBlock}. Does not register it - use
     * {@link at.woodexplosive.woodlib.api.block.CustomBlockRegistry#register(ICustomBlock, org.bukkit.inventory.ItemStack)}.
     * @return the new {@link ICustomBlock}
     * @throws IllegalArgumentException if a port isn't on a cell of the shape, or the shape is too large
     */
    @Contract(value = "-> new")
    @NotNull ICustomBlock build();
}
