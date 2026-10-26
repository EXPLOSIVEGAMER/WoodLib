package at.woodexplosive.woodlib.api.block.event;

import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.PlacementContext;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player is about to place a {@link ICustomBlock} by right-clicking with a linked item,
 * after {@link ICustomBlock#canPlace(PlacementContext)} passed. Cancelling the event prevents the
 * structure from being placed and the item from being consumed.
 */
public class CustomBlockPlaceEvent extends CustomBlockEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final PlacementContext placement;
    private final ItemStack itemInHand;

    private boolean cancelled = false;

    /**
     * @param placement the resolved placement
     * @param itemInHand the linked placer item in the player's hand
     */
    public CustomBlockPlaceEvent(@NotNull PlacementContext placement, @NotNull ItemStack itemInHand) {
        super(placement.player(), placement.instance());
        this.placement = placement;
        this.itemInHand = itemInHand;
    }

    /**
     * @return the resolved placement: clicked block and face plus the prospective structure
     */
    public @NotNull PlacementContext getPlacement() {
        return placement;
    }

    /**
     * @return the block the player right-clicked to trigger this placement
     */
    public @NotNull Block getClickedBlock() {
        return placement.clickedBlock();
    }

    /**
     * @return the face of {@link #getClickedBlock()} that was clicked
     */
    public @NotNull BlockFace getClickedFace() {
        return placement.clickedFace();
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
