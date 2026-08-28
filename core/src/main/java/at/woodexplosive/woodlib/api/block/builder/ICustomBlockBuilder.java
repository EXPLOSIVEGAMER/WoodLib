package at.woodexplosive.woodlib.api.block.builder;

import at.woodexplosive.woodlib.api.block.CustomBlockPart;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.ToolTier;
import at.woodexplosive.woodlib.api.block.event.CustomBlockBreakEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockInteractEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockPlaceEvent;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface ICustomBlockBuilder<T extends ICustomBlockBuilder<T>> {

    /**
     * Adds a part to this structure.
     * @param part the {@link CustomBlockPart} to add
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    @NotNull T part(@NotNull CustomBlockPart part);

    /**
     * Sets whether this CustomBlock is placed with a 4-way cardinal rotation snapped from the placing
     * player's yaw. Defaults to {@code false}.
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

    @Contract(value = "_ -> this")
    @NotNull T setOnBlockInteract(@NotNull Consumer<CustomBlockInteractEvent> event);

    @Contract(value = "_ -> this")
    @NotNull T setOnBlockBreakEvent(@NotNull Consumer<CustomBlockBreakEvent> event);

    @Contract(value = "_ -> this")
    @NotNull T setOnBlockPlaceEvent(@NotNull Consumer<CustomBlockPlaceEvent> event);

    /**
     * Builds the configured {@link ICustomBlock}. Does not register it - use
     * {@link at.woodexplosive.woodlib.api.block.CustomBlockRegistry#register(ICustomBlock, org.bukkit.inventory.ItemStack)}.
     * @return the new {@link ICustomBlock}
     * @throws IllegalStateException if no parts were added
     */
    @Contract(value = "-> new")
    @NotNull ICustomBlock build();
}
