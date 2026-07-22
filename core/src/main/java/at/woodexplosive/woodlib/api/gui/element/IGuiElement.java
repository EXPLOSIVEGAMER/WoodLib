package at.woodexplosive.woodlib.api.gui.element;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.gui.event.GuiClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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

    /**
     * Returns the shared no-op {@link ClickCallback}, typed for the given GUI.
     */
    ClickCallback EMPTY_CALLBACK = event -> false;

    /**
     * The key an element's {@link #getId()} is stamped under on its display {@link ItemStack} (see
     * {@link #getItemStackForDisplay()}), so a GUI can resolve the element - and its callback - from
     * whichever slot the physical item ends up in after being dragged, shift-clicked, hotbar-swapped, etc.
     * @return the id {@link NamespacedKey}
     */
    @Contract(pure = true)
    static NamespacedKey idKey() {
        return new NamespacedKey(WoodLib.plugin(), "gui_element_id");
    }

    /**
     * Returns this element's click callback.
     * @return the {@link ClickCallback}
     */
    @Contract(pure = true)
    ClickCallback getCallback();

    /**
     * A stable identity for this element instance. Stamped onto {@link #getItemStackForDisplay()} so
     * the element can still be found after its item physically moves to a different slot.
     * @return the element's id
     */
    @Contract(pure = true)
    UUID getId();

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
        ItemStack stack = this.getItemStack().clone();
        if (this.hasCallback()) {
            stack.editMeta(meta -> meta.getPersistentDataContainer().set(idKey(), PersistentDataType.STRING, this.getId().toString()));
        }
        return stack;
    }

    /** Callback invoked when an element (or slot) is clicked inside a GUI. */
    @FunctionalInterface
    interface ClickCallback {

        /**
         * Handles a click on the element.
         * @param event the underlying {@link GuiClickEvent}
         * @return {@code true} if the click event should be canceled
         */
        boolean click(@NotNull GuiClickEvent event);
    }
}
