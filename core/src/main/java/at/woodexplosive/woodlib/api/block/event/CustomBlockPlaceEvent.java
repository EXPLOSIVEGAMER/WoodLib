package at.woodexplosive.woodlib.api.block.event;

import at.woodexplosive.woodlib.api.block.ICustomBlock;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player is about to place a {@link ICustomBlock} by right-clicking with a linked item.
 * Cancelling the event prevents the structure from being placed and the item from being consumed.
 */
public class CustomBlockPlaceEvent extends CustomBlockEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Block clickedBlock;
    private final BlockFace clickedFace;
    private final int rotationSteps;
    private final ItemStack itemInHand;

    private boolean cancelled = false;

    /**
     * @param player the placing player
     * @param customBlock the {@link ICustomBlock} being placed
     * @param originBlock the structure's placement origin block
     * @param clickedBlock the block the player right-clicked
     * @param clickedFace the face of {@code clickedBlock} that was clicked
     * @param rotationSteps the structure's rotation (0-3, 90° steps), always {@code 0} unless {@link ICustomBlock#rotatable()}
     * @param itemInHand the linked placer item in the player's hand
     */
    public CustomBlockPlaceEvent(@NotNull Player player, @NotNull ICustomBlock customBlock, @NotNull Block originBlock,
                                  @NotNull Block clickedBlock, @NotNull BlockFace clickedFace, int rotationSteps,
                                  @NotNull ItemStack itemInHand) {
        super(player, customBlock, originBlock);
        this.clickedBlock = clickedBlock;
        this.clickedFace = clickedFace;
        this.rotationSteps = rotationSteps;
        this.itemInHand = itemInHand;
    }

    /**
     * @return the block the player right-clicked to trigger this placement
     */
    public @NotNull Block getClickedBlock() {
        return clickedBlock;
    }

    /**
     * @return the face of {@link #getClickedBlock()} that was clicked
     */
    public @NotNull BlockFace getClickedFace() {
        return clickedFace;
    }

    /**
     * @return the structure's rotation in 90° steps (0-3)
     */
    public int getRotationSteps() {
        return rotationSteps;
    }

    /**
     * @return the linked placer item in the player's hand
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
