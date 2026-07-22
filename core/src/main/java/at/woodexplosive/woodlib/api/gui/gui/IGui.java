package at.woodexplosive.woodlib.api.gui.gui;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A chest-like inventory GUI backed by an {@link org.bukkit.inventory.Inventory} whose holder is the
 * GUI itself, so events can be routed back to it.
 *
 * <p>GUIs are built through their {@code Builder} (see
 * {@link at.woodexplosive.woodlib.gui.gui.SimpleGui}), populated with {@link IGuiElement}s per slot,
 * and shown with {@link #open(Player)}. While open, a GUI ticks every server tick (see
 * {@link #startTicking()}).</p>
 *
 * @param <T> the concrete GUI type, for fluent self-returning methods (CRTP)
 */
public interface IGui<T extends IGui<T>> extends InventoryHolder {

    /**
     * A no-op {@link Callback} that never cancels its event.
     *
     * @param <E> the event type
     * @return a callback returning {@code false}
     */
    static <E extends Event> IGui.Callback<E> emptyCallback() {
        return event -> false;
    }

    /**
     * A no-op {@link TickCallback} that does nothing.
     *
     * @param <T> the concrete GUI type
     * @return a callback that does nothing
     */
    static <T extends IGui<T>> TickCallback<T> emptyTickCallback() {
        return gui -> {};
    }

    /** Shared {@link MiniMessage} instance for deserializing user-facing strings. */
    MiniMessage MM = MiniMessage.miniMessage();

    /**
     * Returns the inventory title.
     * @return the title
     */
    @Contract(pure = true)
    @NotNull Component getTitle();

    /**
     * Stores the element at the given slot index.
     * @param index the slot index the element is placed at
     * @param element the {@link IGuiElement} to insert
     * @return this GUI for chaining
     */
    T setSlot(int index, @NotNull IGuiElement element);

    /**
     * Stores multiple elements at once, keyed by their slot index.
     * @param elements a map of slot index to {@link IGuiElement}
     * @return this GUI for chaining
     */
    T setSlots(Map<Integer, @NotNull IGuiElement> elements);

    /**
     * Sets a standalone click callback for the given slot, independent of the element placed there.
     * @param index the slot index
     * @param callback the {@link IGuiElement.ClickCallback} to run on click
     * @return this GUI for chaining
     */
    T setSlotCallback(int index, @NotNull IGuiElement.ClickCallback callback);

    /**
     * Sets standalone click callbacks for multiple slots at once.
     * @param callbacks a map of slot index to {@link IGuiElement.ClickCallback}
     * @return this GUI for chaining
     */
    T setSlotCallbacks(Map<Integer, IGuiElement.@NotNull ClickCallback> callbacks);

    /**
     * Stores the element in the first empty slot of the GUI.
     * @param element the {@link IGuiElement} to insert
     * @return this GUI for chaining
     */
    T addSlot(@NotNull IGuiElement element);

    /**
     * Stores each element in the next free slot, in order.
     * @param elements the elements to insert
     * @return this GUI for chaining
     */
    T addSlots(@NotNull List<IGuiElement> elements);

    /**
     * Varargs variant of {@link #addSlots(List)}.
     * @param elements the elements to insert
     * @return this GUI for chaining
     */
    default T addSlots(@NotNull IGuiElement... elements) {
        return this.addSlots(List.of(elements));
    }

    /**
     * Closes the inventory for all viewers.
     * @return the number of viewers the inventory was closed for
     */
    int close();

    /**
     * Opens an inventory window with the specified inventory on the top and the player's inventory on the bottom.
     * @param player The player
     * @return The newly opened {@link InventoryView}
     */
    @Nullable InventoryView open(@NotNull Player player);

    /** Starts the per-tick update task for this GUI (fires {@link at.woodexplosive.woodlib.api.gui.event.GuiTickEvent} every tick). */
    void startTicking();

    /** Stops the per-tick update task for this GUI. */
    void stopTicking();

    /**
     * Returns the player this GUI was last {@link #open(Player) opened} for.
     * @return the viewing {@link Player}, or {@code null} if never opened
     */
    @Contract(pure = true)
    @Nullable Player getPlayer();

    /** Re-opens the GUI for its current {@link #getPlayer() player} to reflect changed contents. */
    default void redraw() {
        if (this.getPlayer() == null) return;
        this.open(this.getPlayer());
    }

    /**
     * A cancellable callback bound to a specific inventory {@link Event}.
     * @param <T> the event type
     */
    @FunctionalInterface
    interface Callback<T extends Event> {
        /**
         * Runs the callback and returns success as boolean
         * @param event Inventory Event of the Callback
         * @return if the event should be canceled
         */
        boolean run(@NotNull T event);
    }

    /**
     * A callback invoked once per server tick while the GUI is open.
     * @param <T> the concrete GUI type
     */
    @FunctionalInterface
    interface TickCallback<T extends IGui<T>> {
        /**
         * Runs the tick logic.
         * @param gui the ticking GUI
         */
        void run(@NotNull T gui);
    }
}
