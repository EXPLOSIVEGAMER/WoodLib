package at.woodexplosive.woodlib.api.block;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * One placed "cell" of an {@link ICustomBlock}'s structure: a collision block at a position
 * relative to the structure's placement origin, plus the {@link DisplayDefinition}s rendered there.
 *
 * @param offset the position of this part relative to the structure's origin (the block a player
 *               targeted, offset by the clicked face); {@code (0,0,0)} for a single-block structure's
 *               only part
 * @param transformation a part-wide transformation combined onto every {@link DisplayDefinition} in
 *                        {@code displays} (translations add, rotations compose, scales multiply)
 * @param barrierData the block data placed for collision, typically {@link Material#BARRIER}; its
 *                    state (e.g. a stair's facing) is rotated along with the structure
 * @param displays the {@link DisplayDefinition}s spawned at this part's position
 */
public record CustomBlockPart(@NotNull BlockVector offset, @NotNull Transformation transformation, @NotNull BlockData barrierData,
                              @NotNull List<DisplayDefinition> displays) {

    public CustomBlockPart {
        barrierData = barrierData.clone();
        displays = List.copyOf(displays);
    }

    /**
     * Creates a part whose collision block is {@code barrierMaterial} in its default state.
     * @param offset the position of this part relative to the structure's origin
     * @param transformation a part-wide transformation combined onto every display
     * @param barrierMaterial the material placed for collision, typically {@link Material#BARRIER}
     * @param displays the {@link DisplayDefinition}s spawned at this part's position
     */
    public CustomBlockPart(@NotNull BlockVector offset, @NotNull Transformation transformation, @NotNull Material barrierMaterial,
                           @NotNull List<DisplayDefinition> displays) {
        this(offset, transformation, barrierMaterial.createBlockData(), displays);
    }

    /**
     * @return a copy of the block data placed for collision
     */
    @Override
    public @NotNull BlockData barrierData() {
        return barrierData.clone();
    }

    /**
     * @return the material of the collision block
     */
    public @NotNull Material barrierMaterial() {
        return barrierData.getMaterial();
    }

    public static Transformation defaultTransformation() {
        return defaultTransformation(0, 0, 0, 1, 1, 1);
    }

    public static Transformation defaultTransformation(float x, float y, float z) {
        return defaultTransformation(x, y, z, 1, 1, 1);
    }

    public static Transformation defaultTransformation(float x, float y, float z, float sx, float sy, float sz) {
        return new Transformation(new Vector3f(x, y, z), new Quaternionf(), new Vector3f(sx, sy, sz), new Quaternionf());
    }
}
