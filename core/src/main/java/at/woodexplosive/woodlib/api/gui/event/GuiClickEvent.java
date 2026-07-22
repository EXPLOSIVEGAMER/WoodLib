package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired for every click inside a WoodLib {@link IGui}, wrapping the underlying Bukkit
 * {@link InventoryClickEvent} with the GUI and (if resolved) the {@link IGuiElement} that was
 * clicked.
 *
 * <p>Dispatched by {@link at.woodexplosive.woodlib.gui.gui.AbstractGui.GuiListener} for the global
 * click callback, the resolved element's callback, and any standalone slot callback, in that order;
 * cancelling any of those (or this event itself) cancels the underlying inventory click.</p>
 */
@SuppressWarnings("UnstableApiUsage")
public class GuiClickEvent extends InventoryClickEvent implements IGuiEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final IGui<?> gui;
    private final IGuiElement element;

    private boolean cancelled = false;

    /**
     * @param gui the GUI the click occurred in
     * @param element the resolved {@link IGuiElement} for the clicked slot, or {@code null} if none
     *                 could be resolved (e.g. for the global-click callback's event)
     * @param event the underlying Bukkit {@link InventoryClickEvent} this event wraps
     */
    public GuiClickEvent(@NotNull IGui<?> gui, @Nullable IGuiElement element, @NotNull InventoryClickEvent event) {
        super(event.getView(), event.getSlotType(), event.getRawSlot(), event.getClick(), event.getAction(), event.getHotbarButton());
        this.gui = gui;
        this.element = element;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Bukkit handler list accessor.
     * @return the {@link HandlerList}
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Returns the resolved element for the clicked slot.
     * @return the clicked {@link IGuiElement}, or {@code null} if none could be resolved
     */
    public @Nullable IGuiElement getElement() {
        return element;
    }

    /**
     * Whether the click landed outside the inventory (raw slot {@code -999}).
     * @return {@code true} if the click was outside the inventory
     */
    public boolean clickedOutside() {
        return this.getRawSlot() == -999;
    }

    @Override
    public @NotNull IGui<?> getGui() {
        return this.gui;
    }
}
