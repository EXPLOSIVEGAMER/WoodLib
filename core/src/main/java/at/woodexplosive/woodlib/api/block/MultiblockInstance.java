package at.woodexplosive.woodlib.api.block;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.utils.BlockPos;
import at.woodexplosive.woodlib.api.utils.DirPos;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A placed (or, during placement, prospective) {@link ICustomBlock} multiblock: its definition, its
 * core block and the direction its front faces. Maps the structure's {@link LocalPos local frame} onto
 * the world - the WoodLib counterpart of resolving HBM's {@code findCore} and reading the core's
 * rotation metadata.
 */
public final class MultiblockInstance {

    private final ICustomBlock customBlock;
    private final Block core;
    private final BlockFace facing;

    /**
     * @param customBlock the multiblock's definition
     * @param core the core block
     * @param facing the horizontal direction the structure's front faces
     * @throws IllegalArgumentException if {@code facing} isn't one of the four cardinal directions
     */
    public MultiblockInstance(@NotNull ICustomBlock customBlock, @NotNull Block core, @NotNull BlockFace facing) {
        rotationSteps(facing); // validates
        this.customBlock = customBlock;
        this.core = core;
        this.facing = facing;
    }

    /**
     * @return the multiblock's definition
     */
    @Contract(pure = true)
    public @NotNull ICustomBlock customBlock() {
        return customBlock;
    }

    /**
     * @return the core block, holding the structure's identity, facing and {@link #data() data}
     */
    @Contract(pure = true)
    public @NotNull Block core() {
        return core;
    }

    /**
     * @return the horizontal direction the structure's front faces
     */
    @Contract(pure = true)
    public @NotNull BlockFace facing() {
        return facing;
    }

    /**
     * @return {@link #facing()} as clockwise 90° steps from {@link BlockFace#NORTH}, the authoring orientation
     */
    @Contract(pure = true)
    public int rotationSteps() {
        return rotationSteps(facing);
    }

    /**
     * @param pos a position in the structure's local frame
     * @return the world block at {@code pos}
     */
    @Contract(pure = true)
    public @NotNull Block toWorld(@NotNull LocalPos pos) {
        int[] xz = rotateXZ(pos.right(), -pos.forward(), rotationSteps());
        return core.getRelative(xz[0], pos.up(), xz[1]);
    }

    /**
     * @param direction a direction in the structure's local frame
     * @return the matching world {@link BlockFace}
     */
    @Contract(pure = true)
    public @NotNull BlockFace toWorld(@NotNull LocalDirection direction) {
        LocalPos step = direction.step();
        int[] xz = rotateXZ(step.right(), -step.forward(), rotationSteps());
        for (BlockFace face : DirPos.CARDINAL_6) {
            if (face.getModX() == xz[0] && face.getModY() == step.up() && face.getModZ() == xz[1]) return face;
        }
        throw new IllegalStateException("Unreachable: " + direction);
    }

    /**
     * @return every world block of the structure, the core first
     */
    @Contract(pure = true)
    public @NotNull List<Block> cells() {
        List<Block> result = new ArrayList<>();
        for (LocalPos pos : customBlock.shape().cells()) result.add(toWorld(pos));
        return result;
    }

    /**
     * @return the world blocks of every port (a cell with several ports appears once per port)
     */
    @Contract(pure = true)
    public @NotNull List<Block> portBlocks() {
        List<Block> result = new ArrayList<>();
        for (PortDefinition port : customBlock.shape().ports()) result.add(toWorld(port.pos()));
        return result;
    }

    /**
     * The outward connections of every port, in the form {@link at.woodexplosive.woodlib.api.uninode.GenNode#addConnection(DirPos)}
     * expects: the neighbouring position outside the port, and the direction pointing to it.
     * @return one {@link DirPos} per port
     */
    @Contract(pure = true)
    public @NotNull List<DirPos> portConnections() {
        List<DirPos> result = new ArrayList<>();
        for (PortDefinition port : customBlock.shape().ports()) {
            result.add(DirPos.of(BlockPos.of(toWorld(port.pos())), toWorld(port.direction())));
        }
        return result;
    }

    /**
     * @param block a world block
     * @return {@code true} if {@code block} is this structure's core
     */
    @Contract(pure = true)
    public boolean isCore(@NotNull Block block) {
        return block.equals(core);
    }

    /**
     * @param block a world block
     * @return {@code true} if {@code block} is one of this structure's ports
     */
    @Contract(pure = true)
    public boolean isPort(@NotNull Block block) {
        for (PortDefinition port : customBlock.shape().ports()) {
            if (toWorld(port.pos()).equals(block)) return true;
        }
        return false;
    }

    /**
     * The core's persistent block data - the place to keep per-structure state (the counterpart of a
     * TileEntity's NBT). WoodLib's own keys live under the host plugin's namespace; use your own.
     * @return the core's {@link CustomBlockData}
     */
    public @NotNull CustomBlockData data() {
        return new CustomBlockData(core, WoodLib.plugin());
    }

    /**
     * @param facing one of the four cardinal directions
     * @return clockwise 90° steps from {@link BlockFace#NORTH}
     * @throws IllegalArgumentException if {@code facing} isn't cardinal
     */
    @Contract(pure = true)
    public static int rotationSteps(@NotNull BlockFace facing) {
        return switch (facing) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> throw new IllegalArgumentException("Facing must be NORTH, EAST, SOUTH or WEST, got " + facing);
        };
    }

    /**
     * Rotates a horizontal offset clockwise (seen from above) by {@code steps} 90° increments.
     */
    private static int[] rotateXZ(int x, int z, int steps) {
        for (int i = 0; i < steps; i++) {
            int nx = -z;
            z = x;
            x = nx;
        }
        return new int[] { x, z };
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MultiblockInstance other
                && customBlock.id().equals(other.customBlock.id()) && core.equals(other.core) && facing == other.facing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(customBlock.id(), core, facing);
    }

    @Override
    public String toString() {
        return "MultiblockInstance[" + customBlock.id() + " @ " + core.getX() + "," + core.getY() + "," + core.getZ() + " facing " + facing + "]";
    }
}
