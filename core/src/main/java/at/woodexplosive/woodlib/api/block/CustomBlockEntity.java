package at.woodexplosive.woodlib.api.block;

import at.woodexplosive.woodlib.api.uninode.ILoadedEndpoint;
import at.woodexplosive.woodlib.impl.interfaces.Internal;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Per-structure runtime object of a placed {@link ICustomBlock} - the counterpart of HBM's TileEntity.
 * Created by {@link ICustomBlock#createEntity(MultiblockInstance)} and managed entirely by WoodLib:
 * <ul>
 *     <li>created when the structure is placed, and again whenever its core's chunk loads,</li>
 *     <li>{@link #tick() ticked} every server tick while that chunk stays loaded,</li>
 *     <li>{@link #onUnload() unloaded} when the chunk or world unloads or WoodLib shuts down,</li>
 *     <li>{@link #onRemove() removed} when the structure is broken.</li>
 * </ul>
 *
 * <p>The object itself is volatile: anything that must survive a chunk unload or restart belongs in
 * {@link #data()}, the core's persistent data (HBM's TileEntity NBT). Read it in {@link #onLoad()} and
 * write it back in {@link #onUnload()} or whenever it changes.</p>
 *
 * <p>Identity is object identity, so an entity is a stable key for e.g.
 * {@link at.woodexplosive.woodlib.api.uninode.NodeNet#addReceiver network subscriptions}; it reports
 * itself as not {@link #isLoaded() loaded} once unloaded or removed, which makes networks drop it.</p>
 */
public abstract class CustomBlockEntity implements ILoadedEndpoint {

    private final MultiblockInstance instance;
    private boolean loaded;

    /**
     * @param instance the structure this entity belongs to
     */
    protected CustomBlockEntity(@NotNull MultiblockInstance instance) {
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    /**
     * Called once the entity was registered: right after its structure was placed, or after its core's
     * chunk loaded. Restore state from {@link #data()} here. No-op by default.
     */
    public void onLoad() {}

    /**
     * Called every server tick while the entity is loaded (HBM's {@code updateEntity}). Must not assume
     * neighbouring chunks are loaded. No-op by default.
     */
    public void tick() {}

    /**
     * Called when the core's chunk or world unloads, or WoodLib shuts down. The structure itself still
     * exists; persist state to {@link #data()} here. No-op by default.
     */
    public void onUnload() {}

    /**
     * Called when the structure is broken, before its blocks are cleared (e.g. to unsubscribe from
     * networks). {@link #data()} is still readable. No-op by default.
     */
    public void onRemove() {}

    /**
     * @return the structure this entity belongs to
     */
    @Contract(pure = true)
    public final @NotNull MultiblockInstance instance() {
        return instance;
    }

    /**
     * @return the structure's core block
     */
    @Contract(pure = true)
    public final @NotNull Block core() {
        return instance.core();
    }

    /**
     * @return the world the structure is in
     */
    @Contract(pure = true)
    public final @NotNull World world() {
        return instance.core().getWorld();
    }

    /**
     * @return the horizontal direction the structure's front faces
     */
    @Contract(pure = true)
    public final @NotNull BlockFace facing() {
        return instance.facing();
    }

    /**
     * @return the core's persistent data, the place for state that must survive unloads and restarts
     */
    public final @NotNull CustomBlockData data() {
        return instance.data();
    }

    /**
     * @return {@code true} between {@link #onLoad()} and {@link #onUnload()}/{@link #onRemove()}
     */
    @Override
    @Contract(pure = true)
    public final boolean isLoaded() {
        return loaded;
    }

    /**
     * Internal: sets the loaded flag. Called by WoodLib around the lifecycle hooks; do not call it yourself.
     * @param loaded the new state
     */
    @Internal
    public final void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + instance + "]";
    }
}
