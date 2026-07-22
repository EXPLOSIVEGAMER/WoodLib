package at.woodexplosive.woodlib.api.gui.gui.Listener;

import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit {@link Listener} contract for routing inventory events to the owning
 * {@link at.woodexplosive.woodlib.api.gui.gui.IGui}. Implemented by
 * {@link at.woodexplosive.woodlib.gui.gui.AbstractGui.GuiListener}, which WoodLib registers on
 * initialization.
 */
public interface IGuiListener extends Listener {

    /**
     * Handles an inventory being closed.
     * @param event the {@link InventoryCloseEvent}
     */
    void onInventoryClose(@NotNull InventoryCloseEvent event);

    /**
     * Handles an inventory being opened.
     * @param event the {@link InventoryOpenEvent}
     */
    void onInventoryOpen(@NotNull InventoryOpenEvent event);

    /**
     * Handles a click inside an inventory.
     * @param event the {@link InventoryClickEvent}
     */
    void onInventoryClick(@NotNull InventoryClickEvent event);

    /**
     * Handles an item drag across inventory slots.
     * @param event the {@link InventoryDragEvent}
     */
    void onInventoryDrag(@NotNull InventoryDragEvent event);
}
