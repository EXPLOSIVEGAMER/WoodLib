package at.woodexplosive.woodlib.api.gui.element;

import at.woodexplosive.woodlib.api.gui.gui.IGui;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single slot entry in an {@link at.woodexplosive.woodlib.api.gui.gui.IGui}: an {@link ItemStack}
 * to display plus an optional {@link ClickCallback} invoked when the slot is clicked.
 *
 * <p>Instances are typically created through
 * {@link at.woodexplosive.woodlib.gui.element.GuiElement} and its builder.</p>
 */
public interface IGuiElement {
    /** Shared {@link MiniMessage} instance for deserializing user-facing strings. */
    MiniMessage MM = MiniMessage.miniMessage();

    /** A shared no-op {@link ClickCallback} that never cancels the click. */
    ClickCallback EMPTY_CALLBACK = (event, gui, element, clickType, action) -> false;

    /**
     * Returns this element's click callback.
     * @return the {@link ClickCallback}
     */
    @Contract(pure = true)
    ClickCallback getCallback();

    /**
     * Creates an independent copy of this element (item and callback).
     * @return a new {@link IGuiElement}
     */
    @Contract(value = "-> new", pure = true)
    IGuiElement copy();

    /**
     * Whether this element has a real (non-empty) click callback.
     * @return {@code true} if a callback other than {@link #EMPTY_CALLBACK} is set
     */
    @Contract(pure = true)
    default boolean hasCallback() {
        return this.getCallback() != null && this.getCallback() != EMPTY_CALLBACK;
    }

    /**
     * Returns this element's backing {@link ItemStack} (not a copy).
     * @return the {@link ItemStack}
     */
    @Contract(pure = true)
    ItemStack getItemStack();

    /**
     * Returns a copy of this element's item, safe to place into an inventory for display.
     * @return a copy of {@link #getItemStack()}
     */
    @Contract(pure = true)
    default ItemStack getItemStackForDisplay() {
        return this.getItemStack().clone();
    }

    /** Callback invoked when an element (or slot) is clicked inside a GUI. */
    @FunctionalInterface
    interface ClickCallback {

        /**
         * Handles a click on the element.
         * @param event the underlying {@link InventoryClickEvent}
         * @param clickedGui the GUI that was clicked
         * @param element the clicked {@link IGuiElement}, or {@code null} for a bare slot callback
         * @param clickType the {@link ClickType} of the click
         * @param action the {@link InventoryAction} the click would trigger
         * @return {@code true} if the click event should be canceled
         */
        boolean click(@NotNull InventoryClickEvent event, @NotNull IGui<?> clickedGui, @Nullable IGuiElement element, @NotNull ClickType clickType, @NotNull InventoryAction action);
    }
}
