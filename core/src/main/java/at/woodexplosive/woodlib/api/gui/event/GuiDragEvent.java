package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.gui.IGui;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class GuiDragEvent extends InventoryDragEvent implements IGuiEvent {

    private final IGui<?> gui;

    public GuiDragEvent(@NotNull InventoryView view, @Nullable ItemStack newCursor, @NotNull ItemStack oldCursor, boolean right, @NotNull Map<Integer, ItemStack> slots, IGui<?> gui) {
        super(view, newCursor, oldCursor, right, slots);
        this.gui = gui;
    }

    @Override
    public @NotNull IGui<?> getGui() {
        return this.gui;
    }
}
