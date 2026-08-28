package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.gui.IGui;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a WoodLib {@link IGui} is opened for a player, extending the native
 * {@link InventoryOpenEvent} with the GUI it originated from.
 */
@SuppressWarnings("UnstableApiUsage")
public class GuiOpenEvent extends InventoryOpenEvent implements IGuiEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final IGui<?> gui;

    /**
     * @param transaction the underlying inventory view being opened
     * @param gui the GUI being opened
     */
    public GuiOpenEvent(@NotNull InventoryView transaction, IGui<?> gui) {
        super(transaction);
        this.gui = gui;
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
    public @NotNull IGui<?> getGui() {
        return this.gui;
    }
}
