package at.woodexplosive.woodlib.api.block.event;

import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.MultiblockInstance;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all Bukkit events fired by WoodLib's CustomBlock system. Carries the player who
 * triggered the interaction and the {@link MultiblockInstance} involved.
 */
public abstract class CustomBlockEvent extends Event {

    private final Player player;
    private final MultiblockInstance instance;

    /**
     * @param player the player who triggered the interaction
     * @param instance the structure involved
     */
    protected CustomBlockEvent(@NotNull Player player, @NotNull MultiblockInstance instance) {
        this.player = player;
        this.instance = instance;
    }

    /**
     * @return the player who triggered the interaction
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * @return the structure involved: definition, core and facing
     */
    public @NotNull MultiblockInstance getInstance() {
        return instance;
    }

    /**
     * @return the {@link ICustomBlock} definition involved
     */
    public @NotNull ICustomBlock getCustomBlock() {
        return instance.customBlock();
    }

    /**
     * @return the structure's core block (the anchor every {@link at.woodexplosive.woodlib.api.block.LocalPos} is relative to)
     */
    public @NotNull Block getCoreBlock() {
        return instance.core();
    }

    /**
     * @return the horizontal direction the structure's front faces
     */
    public @NotNull BlockFace getFacing() {
        return instance.facing();
    }

    /**
     * @return the World
     */
    public @NotNull World getWorld() {
        return instance.core().getWorld();
    }
}
