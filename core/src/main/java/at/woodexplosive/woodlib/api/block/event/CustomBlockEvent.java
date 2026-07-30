package at.woodexplosive.woodlib.api.block.event;

import at.woodexplosive.woodlib.api.block.ICustomBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all Bukkit events fired by WoodLib's CustomBlock system. Carries the player who
 * triggered the interaction, the {@link ICustomBlock} definition involved, and the structure's
 * placement origin block.
 */
public abstract class CustomBlockEvent extends Event {

    private final Player player;
    private final ICustomBlock customBlock;
    private final Block originBlock;

    /**
     * @param player the player who triggered the interaction
     * @param customBlock the {@link ICustomBlock} definition involved
     * @param originBlock the structure's placement origin block
     */
    protected CustomBlockEvent(@NotNull Player player, @NotNull ICustomBlock customBlock, @NotNull Block originBlock) {
        this.player = player;
        this.customBlock = customBlock;
        this.originBlock = originBlock;
    }

    /**
     * @return the player who triggered the interaction
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * @return the {@link ICustomBlock} definition involved
     */
    public @NotNull ICustomBlock getCustomBlock() {
        return customBlock;
    }

    /**
     * @return the structure's placement origin block (the anchor every {@link at.woodexplosive.woodlib.api.block.CustomBlockPart#offset()} is relative to)
     */
    public @NotNull Block getOriginBlock() {
        return originBlock;
    }
}
