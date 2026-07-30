package at.woodexplosive.woodlib.api.block.event;

import at.woodexplosive.woodlib.api.block.ICustomBlock;
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
    private final int clickedPartIndex;
    private final int rotationSteps;
    private final EquipmentSlot hand;
    private final ItemStack itemInHand;

    private boolean cancelled = false;

    /**
     * @param player the interacting player
     * @param customBlock the {@link ICustomBlock} being interacted with
     * @param originBlock the structure's placement origin block
     * @param clickedBlock the specific part block that was right-clicked
     * @param clickedPartIndex the index into {@link ICustomBlock#parts()} of {@code clickedBlock}
     * @param rotationSteps the structure's rotation (0-3, 90° steps) as placed
     * @param hand the hand used to interact
     * @param itemInHand the item in that hand, possibly empty
     */
    public CustomBlockInteractEvent(@NotNull Player player, @NotNull ICustomBlock customBlock, @NotNull Block originBlock,
                                     @NotNull Block clickedBlock, int clickedPartIndex, int rotationSteps,
                                     @NotNull EquipmentSlot hand, @NotNull ItemStack itemInHand) {
        super(player, customBlock, originBlock);
        this.clickedBlock = clickedBlock;
        this.clickedPartIndex = clickedPartIndex;
        this.rotationSteps = rotationSteps;
        this.hand = hand;
        this.itemInHand = itemInHand;
    }

    /**
     * @return the specific part block that was right-clicked
     */
    public @NotNull Block getClickedBlock() {
        return clickedBlock;
    }

    /**
     * @return the index into {@link ICustomBlock#parts()} of {@link #getClickedBlock()}
     */
    public int getClickedPartIndex() {
        return clickedPartIndex;
    }

    /**
     * @return the structure's rotation in 90° steps (0-3), as it was placed
     */
    public int getRotationSteps() {
        return rotationSteps;
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
