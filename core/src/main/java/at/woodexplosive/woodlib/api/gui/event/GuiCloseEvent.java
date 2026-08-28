package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.gui.GuiExitFlag;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a WoodLib {@link IGui} is closed, extending the native
 * {@link InventoryCloseEvent} with the GUI and the {@link GuiExitFlag} it closed with.
 */
@SuppressWarnings("UnstableApiUsage")
public class GuiCloseEvent extends InventoryCloseEvent implements IGuiEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final IGui<?> gui;
    private final byte exitFlag;

    /**
     * @param transaction the underlying inventory view being closed
     * @param gui the GUI being closed
     */
    public GuiCloseEvent(@NotNull InventoryView transaction, IGui<?> gui) {
        super(transaction);
        this.gui = gui;
        this.exitFlag = gui.getExitFlag();
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

    /**
     * @return the raw {@link GuiExitFlag} byte the GUI was closed with (see {@link GuiExitFlag#getFlags(byte)})
     */
    public byte getExitFlag() {
        return this.exitFlag;
    }

    @Override
    public @NotNull IGui<?> getGui() {
        return this.gui;
    }
}
