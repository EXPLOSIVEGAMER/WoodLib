package at.woodexplosive.woodlib.api.block;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * An axis-aligned box of cells around a multiblock's core, measured in blocks per side - the WoodLib
 * counterpart of HBM's {@code getDimensions()} array. Every value counts the cells <i>beyond</i> the
 * core in that direction, so {@link #NONE} is a single block.
 *
 * <p>Directions are relative to the structure's facing (see {@link LocalPos}): {@code forward} is the
 * side its front points to (towards the placing player), {@code left}/{@code right} are from the
 * structure's own point of view.</p>
 *
 * @param up cells above the core
 * @param down cells below the core
 * @param forward cells in front of the core
 * @param backward cells behind the core
 * @param left cells to the structure's left
 * @param right cells to the structure's right
 */
public record Dimensions(int up, int down, int forward, int backward, int left, int right) {

    /** A single cell - just the core. */
    public static final Dimensions NONE = new Dimensions(0, 0, 0, 0, 0, 0);

    public Dimensions {
        if (up < 0 || down < 0 || forward < 0 || backward < 0 || left < 0 || right < 0) {
            throw new IllegalArgumentException("Dimensions must not be negative: " + Arrays.toString(new int[] { up, down, forward, backward, left, right }));
        }
    }

    /**
     * Creates dimensions from an HBM-style array.
     * @param dims exactly six values in the order up, down, forward, backward, left, right
     * @return the dimensions
     * @throws IllegalArgumentException if {@code dims} doesn't have exactly six values
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Dimensions of(int @NotNull [] dims) {
        if (dims.length != 6) throw new IllegalArgumentException("Dimensions need exactly 6 values, got " + dims.length);
        return new Dimensions(dims[0], dims[1], dims[2], dims[3], dims[4], dims[5]);
    }

    /**
     * @param direction the local direction
     * @return how many cells this box extends beyond the core in {@code direction}
     */
    @Contract(pure = true)
    public int extent(@NotNull LocalDirection direction) {
        return switch (direction) {
            case UP -> up;
            case DOWN -> down;
            case FORWARD -> forward;
            case BACKWARD -> backward;
            case LEFT -> left;
            case RIGHT -> right;
        };
    }
}
