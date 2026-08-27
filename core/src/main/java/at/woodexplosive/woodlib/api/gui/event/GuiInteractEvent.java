package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.gui.IGui;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class GuiInteractEvent extends InventoryInteractEvent implements IGuiEvent {
    private final IGui<?> gui;

    public GuiInteractEvent(@NotNull InventoryView transaction, IGui<?> gui) {
        super(transaction);
        this.gui = gui;
    }

    @Override
    public @NotNull IGui<?> getGui() {
        return this.gui;
    }
}
