package at.woodexplosive.woodlib.api.block.event;

import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.MultiblockInstance;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player is about to break a placed {@link ICustomBlock} structure by mining any of its
 * cells. Cancelling the event leaves the structure fully intact.
 */
public class CustomBlockBreakEvent extends CustomBlockEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Block clickedBlock;
    private final boolean dropping;

    private boolean cancelled = false;

    /**
     * @param player the breaking player
     * @param instance the structure being broken
     * @param clickedBlock the specific cell that was mined
     */
    public CustomBlockBreakEvent(@NotNull Player player, @NotNull MultiblockInstance instance, @NotNull Block clickedBlock) {
        super(player, instance);
        this.clickedBlock = clickedBlock;
        this.dropping = player.getGameMode() != GameMode.CREATIVE;
    }

    /**
     * @return the specific cell that was mined to trigger this break
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
     * @return {@code true} if the registered drop item will be given to the player (i.e. they are not
     *         in creative mode)
     */
    public boolean isDropping() {
        return dropping;
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
