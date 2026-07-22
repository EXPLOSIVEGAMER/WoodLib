package at.woodexplosive.woodlib.api.gui.gui.builder;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.event.GuiTickEvent;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Fluent builder contract for {@link IGui}s: register the event callbacks and behavior flags, then
 * produce the GUI via {@link #build()}.
 *
 * @param <T> the concrete builder type, for fluent self-returning methods (CRTP)
 * @param <G> the GUI type produced by this builder
 */
public interface IGuiBuilder<T extends IGuiBuilder<T, G>, G extends IGui<G>> {

    /**
     * Sets the callback run when the GUI is closed.
     * @see InventoryCloseEvent
     * @param onClose the close callback
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T setOnClose(final @NotNull IGui.Callback<InventoryCloseEvent> onClose);

    /**
     * Sets the callback run when the GUI is opened.
     * @see InventoryOpenEvent
     * @param onOpen the open callback
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T setOnOpen(final @NotNull IGui.Callback<InventoryOpenEvent> onOpen);

    /**
     * Sets the callback run when items are dragged across the GUI.
     * @see InventoryDragEvent
     * @param onDrag the drag callback
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T setOnDrag(final @NotNull IGui.Callback<InventoryDragEvent> onDrag);

    /**
     * Sets the callback run every server tick while the GUI is open.
     * @see GuiTickEvent
     * @param onTick the tick callback
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T setOnTick(final @NotNull IGui.Callback<GuiTickEvent<G>> onTick);

    /**
     * Sets a global click callback run for every click in the GUI, before per-slot callbacks.
     * @param onClick the {@link IGuiElement.ClickCallback}
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T setOnClickGlobal(@NotNull IGuiElement.ClickCallback onClick);

    /**
     * Sets the player-manipulation flag.
     * @param playerManipulation {@code true} allows the player to move items in the inventory,
     *                            {@code false} prevents it
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T setPlayerManipulation(boolean playerManipulation);

    /**
     * Builds the configured GUI.
     * @return the built GUI
     */
    @Contract(value = "-> new", pure = true)
    @NotNull G build();
}
