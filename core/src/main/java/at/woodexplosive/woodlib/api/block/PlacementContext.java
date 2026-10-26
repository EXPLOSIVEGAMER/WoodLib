package at.woodexplosive.woodlib.api.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Where and how a player is about to place an {@link ICustomBlock}, as resolved from their click
 * (see {@link ICustomBlock#canPlace(PlacementContext)}).
 *
 * @param player the placing player
 * @param clickedBlock the block the player right-clicked
 * @param clickedFace the clicked face of {@code clickedBlock}
 * @param instance the prospective structure: definition, core position and facing
 */
public record PlacementContext(@NotNull Player player, @NotNull Block clickedBlock, @NotNull BlockFace clickedFace,
                               @NotNull MultiblockInstance instance) {

    /**
     * @return the block next to the clicked face - the cell the player aimed at
     */
    public @NotNull Block target() {
        return clickedBlock.getRelative(clickedFace);
    }
}
