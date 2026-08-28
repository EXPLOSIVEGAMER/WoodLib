package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.gui.IGui;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Fired when a player drags an item across slots of a WoodLib {@link IGui}, extending the native
 * {@link InventoryDragEvent} with the GUI it originated from.
 */
@SuppressWarnings("UnstableApiUsage")
public class GuiDragEvent extends InventoryDragEvent implements IGuiEvent {

    private final IGui<?> gui;

    /**
     * @param view the underlying inventory view being dragged in
     * @param newCursor the item that will be on the cursor after the drag, or {@code null} for none
     * @param oldCursor the item that was on the cursor before the drag
     * @param right whether this was a right-click drag (spreads one item per slot) rather than left-click
     * @param slots the slots affected by the drag, mapped to their resulting item
     * @param gui the GUI the drag occurred in
     */
    public GuiDragEvent(@NotNull InventoryView view, @Nullable ItemStack newCursor, @NotNull ItemStack oldCursor, boolean right, @NotNull Map<Integer, ItemStack> slots, IGui<?> gui) {
        super(view, newCursor, oldCursor, right, slots);
        this.gui = gui;
    }

    @Override
    public @NotNull IGui<?> getGui() {
        return this.gui;
    }
}
