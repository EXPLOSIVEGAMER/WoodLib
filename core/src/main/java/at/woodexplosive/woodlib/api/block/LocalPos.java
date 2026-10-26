package at.woodexplosive.woodlib.api.block;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A cell position relative to a multiblock's core, in the structure's own frame: the core is
 * {@code (0, 0, 0)}, {@code forward} points where the structure's front faces and {@code right} is to its
 * right (clockwise of the facing, seen from above).
 *
 * <p>Displays and collision data are authored for a structure facing {@link org.bukkit.block.BlockFace#NORTH
 * NORTH}, where this frame maps onto the world as {@code x = right, y = up, z = -forward}.</p>
 *
 * @param right cells to the structure's right (negative: left)
 * @param up cells above the core (negative: below)
 * @param forward cells in front of the core (negative: behind)
 */
public record LocalPos(int right, int up, int forward) {

    /** The core's own position. */
    public static final LocalPos CORE = new LocalPos(0, 0, 0);

    /**
     * @param other the offset to add
     * @return this position shifted by {@code other}
     */
    @Contract(value = "_ -> new", pure = true)
    public @NotNull LocalPos add(@NotNull LocalPos other) {
        return new LocalPos(right + other.right, up + other.up, forward + other.forward);
    }

    /**
     * @param direction the direction to step in
     * @return the neighbouring position in {@code direction}
     */
    @Contract(value = "_ -> new", pure = true)
    public @NotNull LocalPos relative(@NotNull LocalDirection direction) {
        return add(direction.step());
    }
}
