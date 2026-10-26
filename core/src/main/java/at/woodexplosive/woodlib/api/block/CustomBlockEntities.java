package at.woodexplosive.woodlib.api.block;

import at.woodexplosive.woodlib.impl.block.CustomBlockEntityManager;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Lookup of the loaded {@link CustomBlockEntity CustomBlockEntities} - the counterpart of
 * {@code world.getTileEntity(x, y, z)} in HBM.
 */
public final class CustomBlockEntities {

    private CustomBlockEntities() {}

    /**
     * @param instance a placed structure
     * @return its loaded entity, or {@code null} if it has none or its chunk isn't loaded
     */
    @Contract(pure = true)
    public static @Nullable CustomBlockEntity get(@NotNull MultiblockInstance instance) {
        return CustomBlockEntityManager.get(instance.core());
    }

    /**
     * Resolves any cell of a structure - core, dummy or port - to the structure's loaded entity.
     * @param cell a cell of a placed structure
     * @return the loaded entity, or {@code null} if {@code cell} isn't a CustomBlock cell or has none
     */
    public static @Nullable CustomBlockEntity get(@NotNull Block cell) {
        return CustomBlockEntityManager.resolve(cell);
    }

    /**
     * Like {@link #get(Block)}, but only returns an entity of the given type.
     * @param cell a cell of a placed structure
     * @param type the expected entity type
     * @return the loaded entity, or {@code null} if there is none or it isn't a {@code type}
     * @param <T> the entity type
     */
    public static <T extends CustomBlockEntity> @Nullable T get(@NotNull Block cell, @NotNull Class<T> type) {
        CustomBlockEntity entity = get(cell);
        return type.isInstance(entity) ? type.cast(entity) : null;
    }
}
