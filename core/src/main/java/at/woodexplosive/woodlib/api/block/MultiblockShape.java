package at.woodexplosive.woodlib.api.block;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The immutable cell layout of an {@link ICustomBlock}: one or more {@link Box boxes} around the core
 * (HBM's {@code getAllDimensions()}), plus the {@link PortDefinition ports} among those cells.
 *
 * <p>The core cell {@link LocalPos#CORE} is always part of the shape, even if no box covers it.</p>
 */
public final class MultiblockShape {

    /** Upper bound on cells per structure, mirroring HBM's {@code MultiblockHandlerXR} safety limit. */
    public static final int MAX_CELLS = 2000;

    /** The shape of a plain single-block CustomBlock. */
    public static final MultiblockShape SINGLE = of(Dimensions.NONE);

    private final List<Box> boxes;
    private final List<PortDefinition> ports;
    private final Set<LocalPos> cells;
    private final Dimensions bounds;

    private MultiblockShape(@NotNull List<Box> boxes, @NotNull List<PortDefinition> ports) {
        this.boxes = List.copyOf(boxes);
        this.ports = List.copyOf(ports);

        Set<LocalPos> cells = new LinkedHashSet<>();
        cells.add(LocalPos.CORE);
        for (Box box : this.boxes) {
            Dimensions d = box.dimensions();
            LocalPos o = box.offset();
            for (int r = o.right() - d.left(); r <= o.right() + d.right(); r++) {
                for (int u = o.up() - d.down(); u <= o.up() + d.up(); u++) {
                    for (int f = o.forward() - d.backward(); f <= o.forward() + d.forward(); f++) {
                        cells.add(new LocalPos(r, u, f));
                        if (cells.size() > MAX_CELLS) {
                            throw new IllegalArgumentException("MultiblockShape exceeds " + MAX_CELLS + " cells");
                        }
                    }
                }
            }
        }
        this.cells = Collections.unmodifiableSet(cells);

        for (PortDefinition port : this.ports) {
            if (!cells.contains(port.pos())) {
                throw new IllegalArgumentException("Port " + port + " is not a cell of the shape");
            }
        }

        int up = 0, down = 0, forward = 0, backward = 0, left = 0, right = 0;
        for (LocalPos cell : cells) {
            up = Math.max(up, cell.up());
            down = Math.max(down, -cell.up());
            forward = Math.max(forward, cell.forward());
            backward = Math.max(backward, -cell.forward());
            left = Math.max(left, -cell.right());
            right = Math.max(right, cell.right());
        }
        this.bounds = new Dimensions(up, down, forward, backward, left, right);
    }

    /**
     * @param dimensions the single box around the core
     * @return a shape consisting of just that box, without ports
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull MultiblockShape of(@NotNull Dimensions dimensions) {
        return builder().box(dimensions).build();
    }

    /**
     * @return a new, empty {@link Builder}
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * @return the boxes this shape was built from
     */
    @Contract(pure = true)
    public @NotNull List<Box> boxes() {
        return boxes;
    }

    /**
     * @return the declared ports
     */
    @Contract(pure = true)
    public @NotNull List<PortDefinition> ports() {
        return ports;
    }

    /**
     * @return every cell of the structure, the core first
     */
    @Contract(pure = true)
    public @NotNull Set<LocalPos> cells() {
        return cells;
    }

    /**
     * @return the smallest {@link Dimensions} enclosing every cell
     */
    @Contract(pure = true)
    public @NotNull Dimensions bounds() {
        return bounds;
    }

    /**
     * @param pos a cell
     * @return the ports on {@code pos}, empty if it isn't a port
     */
    @Contract(pure = true)
    public @NotNull List<PortDefinition> portsAt(@NotNull LocalPos pos) {
        List<PortDefinition> result = new ArrayList<>();
        for (PortDefinition port : ports) {
            if (port.pos().equals(pos)) result.add(port);
        }
        return result;
    }

    /**
     * One box of a shape: {@link Dimensions} measured around {@code offset} instead of the core.
     * @param dimensions the box's extents around {@code offset}
     * @param offset the box's center, relative to the core
     */
    public record Box(@NotNull Dimensions dimensions, @NotNull LocalPos offset) {}

    /** Fluent builder for {@link MultiblockShape}. */
    public static final class Builder {
        private final List<Box> boxes = new ArrayList<>();
        private final List<PortDefinition> ports = new ArrayList<>();

        private Builder() {}

        /**
         * Adds a box around the core.
         * @param dimensions the box's extents
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public @NotNull Builder box(@NotNull Dimensions dimensions) {
            return box(dimensions, LocalPos.CORE);
        }

        /**
         * Adds a box around {@code offset}.
         * @param dimensions the box's extents
         * @param offset the box's center, relative to the core
         * @return this builder for chaining
         */
        @Contract(value = "_, _ -> this")
        public @NotNull Builder box(@NotNull Dimensions dimensions, @NotNull LocalPos offset) {
            boxes.add(new Box(dimensions, offset));
            return this;
        }

        /**
         * Adds a port.
         * @param pos the port's cell
         * @param direction the side the port connects through
         * @return this builder for chaining
         */
        @Contract(value = "_, _ -> this")
        public @NotNull Builder port(@NotNull LocalPos pos, @NotNull LocalDirection direction) {
            ports.add(new PortDefinition(pos, direction));
            return this;
        }

        /**
         * Adds a port.
         * @param port the port
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public @NotNull Builder port(@NotNull PortDefinition port) {
            ports.add(port);
            return this;
        }

        /**
         * @return the built shape
         * @throws IllegalArgumentException if a port isn't on a cell, or the shape exceeds {@link #MAX_CELLS}
         */
        @Contract(value = "-> new")
        public @NotNull MultiblockShape build() {
            return new MultiblockShape(boxes, ports);
        }
    }
}
