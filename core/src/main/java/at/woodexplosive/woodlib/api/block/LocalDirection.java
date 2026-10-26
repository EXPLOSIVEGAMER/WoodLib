package at.woodexplosive.woodlib.api.block;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A direction relative to a multiblock's facing. {@link #FORWARD} is the direction its front points
 * to; {@link #LEFT}/{@link #RIGHT} are from the structure's own point of view.
 */
public enum LocalDirection {
    UP(0, 1, 0),
    DOWN(0, -1, 0),
    FORWARD(0, 0, 1),
    BACKWARD(0, 0, -1),
    LEFT(-1, 0, 0),
    RIGHT(1, 0, 0);

    private final LocalPos step;

    LocalDirection(int right, int up, int forward) {
        this.step = new LocalPos(right, up, forward);
    }

    /**
     * @return the one-cell step in this direction
     */
    @Contract(pure = true)
    public @NotNull LocalPos step() {
        return step;
    }

    /**
     * @return the opposite direction
     */
    @Contract(pure = true)
    public @NotNull LocalDirection opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case FORWARD -> BACKWARD;
            case BACKWARD -> FORWARD;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
