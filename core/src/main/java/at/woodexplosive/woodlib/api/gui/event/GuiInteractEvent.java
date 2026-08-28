package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.gui.IGui;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

/**
 * Base event for any player interaction with a WoodLib {@link IGui}'s inventory, extending the native
 * {@link InventoryInteractEvent} with the GUI it originated from.
 */
@SuppressWarnings("UnstableApiUsage")
public class GuiInteractEvent extends InventoryInteractEvent implements IGuiEvent {
    private final IGui<?> gui;

    /**
     * @param transaction the underlying inventory view being interacted with
     * @param gui the GUI being interacted with
     */
    public GuiInteractEvent(@NotNull InventoryView transaction, IGui<?> gui) {
        super(transaction);
        this.gui = gui;
    }

    @Override
    public @NotNull IGui<?> getGui() {
        return this.gui;
    }
}
