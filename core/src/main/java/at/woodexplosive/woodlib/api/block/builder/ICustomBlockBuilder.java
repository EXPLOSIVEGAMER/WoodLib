package at.woodexplosive.woodlib.api.block.builder;

import at.woodexplosive.woodlib.api.block.CustomBlockPart;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.event.CustomBlockBreakEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockInteractEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockPlaceEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
