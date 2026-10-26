package at.woodexplosive.woodlib.impl.block;

import at.woodexplosive.woodlib.api.block.Dimensions;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.LocalPos;
import at.woodexplosive.woodlib.api.block.MultiblockShape;
import at.woodexplosive.woodlib.api.block.PortDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Subclassing base for multiblock {@link ICustomBlock}s, modelled after HBM NTM's {@code BlockDummyable}:
 * define {@link #getDimensions()} and {@link #getOffset()}, optionally {@link #getAllDimensions()} for
 * compound shapes and {@link #getPorts()} for connectors, and override any other {@link ICustomBlock}
 * hook ({@code displays()}, {@code collisionData}, {@code modifyFacing}, {@code canPlace}, events, ...).
 *
 * <p>{@link #shape()} is built from these on first use and cached, so they must be constant.</p>
 */
public abstract class BaseCustomBlock implements ICustomBlock {

    private MultiblockShape shape;

    /**
     * The main box around the core (HBM's {@code getDimensions()}).
     * @return the dimensions, {@link Dimensions#NONE} for a single block
     */
    public abstract @NotNull Dimensions getDimensions();

    /**
     * How far the core is pushed away from the placing player on floor/ceiling placement (HBM's
     * {@code getOffset()}). Usually the forward extent of {@link #getDimensions()}.
     * @return the offset in blocks
     */
    public abstract int getOffset();

    /**
     * Every box of the shape (HBM's {@code getAllDimensions()}). Defaults to just
     * {@link #getDimensions()} around the core.
     * @return the boxes
     */
    public @NotNull List<MultiblockShape.Box> getAllDimensions() {
        return List.of(new MultiblockShape.Box(getDimensions(), LocalPos.CORE));
    }

    /**
     * The structure's ports (HBM's "extra" dummies). None by default.
     * @return the ports
     */
    public @NotNull List<PortDefinition> getPorts() {
        return List.of();
    }

    /**
     * Extra vertical shift of the core on placement (HBM's {@code getHeightOffset()}).
     * @return the height offset, {@code 0} by default
     */
    public int getHeightOffset() {
        return 0;
    }

    @Override
    public final @NotNull MultiblockShape shape() {
        if (shape == null) {
            MultiblockShape.Builder builder = MultiblockShape.builder();
            for (MultiblockShape.Box box : getAllDimensions()) builder.box(box.dimensions(), box.offset());
            for (PortDefinition port : getPorts()) builder.port(port);
            shape = builder.build();
        }
        return shape;
    }

    @Override
    public final int offset() {
        return getOffset();
    }

    @Override
    public final int heightOffset() {
        return getHeightOffset();
    }
}
