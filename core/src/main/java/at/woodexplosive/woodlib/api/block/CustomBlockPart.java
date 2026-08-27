package at.woodexplosive.woodlib.api.block;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * One placed "cell" of an {@link ICustomBlock}'s structure: a collision material at a position
 * relative to the structure's placement origin, plus the {@link DisplayDefinition}s rendered there.
 *
 * @param offset the position of this part relative to the structure's origin (the block a player
 *               targeted, offset by the clicked face); {@code (0,0,0)} for a single-block structure's
 *               only part
 * @param transformation a part-wide transformation combined onto every {@link DisplayDefinition} in
 *                        {@code displays} (translations add, rotations compose, scales multiply)
 * @param barrierMaterial the material placed for collision, typically {@link Material#BARRIER}
 * @param displays the {@link DisplayDefinition}s spawned at this part's position
 */
public record CustomBlockPart(@NotNull BlockVector offset, @NotNull Transformation transformation, @NotNull Material barrierMaterial,
                              @NotNull List<DisplayDefinition> displays) {

    public CustomBlockPart {
        displays = List.copyOf(displays);
    }
}
