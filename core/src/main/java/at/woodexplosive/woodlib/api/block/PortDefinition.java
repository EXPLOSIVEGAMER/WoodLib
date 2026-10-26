package at.woodexplosive.woodlib.api.block;

import org.jetbrains.annotations.NotNull;

/**
 * A port of a multiblock - the counterpart of HBM's "extra" dummy blocks: a marked cell that connects
 * to the outside world (pipes, cables, ...) in one direction.
 *
 * @param pos the port's cell, must be part of the structure's {@link MultiblockShape#cells()}
 * @param direction the side of {@code pos} the port connects through
 */
public record PortDefinition(@NotNull LocalPos pos, @NotNull LocalDirection direction) {}
