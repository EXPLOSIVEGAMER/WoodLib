package at.woodexplosive.woodlib.api.block.event;

import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.MultiblockInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player right-clicks a placed {@link ICustomBlock} structure while <b>not</b> holding an
 * item linked to a CustomBlock (holding a linked item always attempts placement instead, see
 * {@link CustomBlockPlaceEvent}). Purely an observer/veto hook - WoodLib performs no default action
 * beyond firing this event; any reaction (e.g. opening a GUI) is entirely up to the listening plugin.
 */
public class CustomBlockInteractEvent extends CustomBlockEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Block clickedBlock;
    private final EquipmentSlot hand;
    private final ItemStack itemInHand;

    private boolean cancelled = false;

    /**
     * @param player the interacting player
     * @param instance the structure being interacted with
     * @param clickedBlock the specific cell that was right-clicked
     * @param hand the hand used to interact
     * @param itemInHand the item in that hand, possibly empty
     */
    public CustomBlockInteractEvent(@NotNull Player player, @NotNull MultiblockInstance instance, @NotNull Block clickedBlock,
                                     @NotNull EquipmentSlot hand, @NotNull ItemStack itemInHand) {
        super(player, instance);
        this.clickedBlock = clickedBlock;
        this.hand = hand;
        this.itemInHand = itemInHand;
    }

    /**
     * @return the specific cell that was right-clicked
     */
    public @NotNull Block getClickedBlock() {
        return clickedBlock;
    }

    /**
     * @return {@code true} if {@link #getClickedBlock()} is the structure's core
     */
    public boolean isCoreClicked() {
        return getInstance().isCore(clickedBlock);
    }

    /**
     * @return {@code true} if {@link #getClickedBlock()} is one of the structure's ports
     */
    public boolean isPortClicked() {
        return getInstance().isPort(clickedBlock);
    }

    /**
     * @return the hand used to interact
     */
    public @NotNull EquipmentSlot getHand() {
        return hand;
    }

    /**
     * @return the item in {@link #getHand()}, possibly empty
     */
    public @NotNull ItemStack getItemInHand() {
        return itemInHand;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Bukkit handler list accessor.
     * @return the {@link HandlerList}
     */
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
