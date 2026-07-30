package at.woodexplosive.woodlib.api.block.event;

import at.woodexplosive.woodlib.api.block.ICustomBlock;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player is about to break a placed {@link ICustomBlock} structure by left-clicking one
 * of its parts. Cancelling the event leaves the structure fully intact.
 */
public class CustomBlockBreakEvent extends CustomBlockEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Block clickedBlock;
    private final int clickedPartIndex;
    private final int rotationSteps;
    private final boolean dropping;

    private boolean cancelled = false;

    /**
     * @param player the breaking player
     * @param customBlock the {@link ICustomBlock} being broken
     * @param originBlock the structure's placement origin block
     * @param clickedBlock the specific part block that was left-clicked
     * @param clickedPartIndex the index into {@link ICustomBlock#parts()} of {@code clickedBlock}
     * @param rotationSteps the structure's rotation (0-3, 90° steps) as placed
     */
    public CustomBlockBreakEvent(@NotNull Player player, @NotNull ICustomBlock customBlock, @NotNull Block originBlock,
                                  @NotNull Block clickedBlock, int clickedPartIndex, int rotationSteps) {
        super(player, customBlock, originBlock);
        this.clickedBlock = clickedBlock;
        this.clickedPartIndex = clickedPartIndex;
        this.rotationSteps = rotationSteps;
        this.dropping = player.getGameMode() != GameMode.CREATIVE;
    }

    /**
     * @return the specific part block that was left-clicked to trigger this break
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
