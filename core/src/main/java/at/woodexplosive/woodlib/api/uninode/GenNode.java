package at.woodexplosive.woodlib.api.uninode;

import at.woodexplosive.woodlib.api.utils.BlockPos;
import at.woodexplosive.woodlib.api.utils.DirPos;
import com.google.common.collect.ImmutableList;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A conductor's (cable, pipe, ...) presence in the {@code UniNodespace}. Nodes live independently of
 * the blocks that created them, so a network survives its chunks being unloaded.
 *
 * <p>A node may span several {@link #getPositions() positions} (multiblocks) and is registered under
 * each of them. Its {@link #getConnections() connections} point at neighbouring positions; a connection
 * is only established if the neighbour node points back (see {@link DirPos}).</p>
 *
 * <p>Nodes merge into {@link NodeNet}s during the nodespace update. Nodes are keyed by position
 * <b>and</b> {@link INetworkProvider}, so nodes of different network types can share a block.</p>
 *
 * @param <N> the network type this node joins
 */
public class GenNode<N extends NodeNet<?, ?, ?>> {
    /** Type key and network factory; compared by reference. */
    protected final @NotNull INetworkProvider<N> provider;
    /** Every block this node occupies. Used as map keys, so never changed after construction. */
    protected final @NotNull ImmutableList<BlockPos> positions;
    /** Neighbour positions this node tries to connect to. */
    protected @NotNull List<DirPos> connections = new ArrayList<>();
    /** The current network, {@code null} until the nodespace update assigned one. */
    protected N net;
    /** {@code true} once the node was removed from the nodespace; its owner must create a new one. */
    protected boolean expired = false;
    /** {@code true} if the node rechecks its neighbours on the next nodespace update. */
    protected boolean recentlyChanged = true;

    /**
     * @param provider the network type this node belongs to; must be a singleton
     * @param positions every block this node occupies, at least one
     * @throws IllegalArgumentException if {@code positions} is empty
     */
    public GenNode(@NotNull INetworkProvider<N> provider, @NotNull BlockPos @NotNull ... positions) {
        if (positions.length == 0) throw new IllegalArgumentException("GenNode needs at least one position");
        this.provider = Objects.requireNonNull(provider, "provider");
        this.positions = ImmutableList.copyOf(positions);
    }

    /**
     * Replaces this node's connections.
     *
     * <p>Only call this before the node is passed to {@code UniNodespace.createNode}. Existing links
     * are not removed, so to change a live node's shape, destroy it and create a new one.</p>
     *
     * @param cons the new connections
     * @return this node, for chaining
     */
    @Contract("_ -> this")
    public @NotNull GenNode<N> setConnections(@NotNull DirPos @NotNull ... cons) {
        this.connections = new ArrayList<>(List.of(cons));
        return this;
    }

    /**
     * Sets the six standard connections, one per face ({@link BlockFace#UP}, {@link BlockFace#DOWN},
     * {@link BlockFace#NORTH}, {@link BlockFace#SOUTH}, {@link BlockFace#WEST}, {@link BlockFace#EAST}).
     * Replaces any existing connections; the same restrictions as {@link #setConnections} apply.
     *
     * @param self the position the connections start from
     * @return this node, for chaining
     */
    @Contract("_ -> this")
    public @NotNull GenNode<N> setStandardConnections(@NotNull BlockPos self) {
        this.setConnections(
                DirPos.of(self, BlockFace.UP),
                DirPos.of(self, BlockFace.DOWN),
                DirPos.of(self, BlockFace.NORTH),
                DirPos.of(self, BlockFace.SOUTH),
                DirPos.of(self, BlockFace.WEST),
                DirPos.of(self, BlockFace.EAST)
        );
        return this;
    }

    /**
     * Adds a single connection. The same restrictions as {@link #setConnections} apply.
     *
     * @param con the connection to add
     * @return this node, for chaining
     */
    @Contract("_ -> this")
    public @NotNull GenNode<N> addConnection(@NotNull DirPos con) {
        this.connections.add(con);
        return this;
    }

    /**
     * Checks whether this node currently belongs to a live network. <b>Always check this before
     * accessing the network:</b> a freshly created node has no network until the next nodespace update,
     * and a node loses its network whenever the network is rebuilt.
     *
     * @return {@code true} if this node has a network that was not destroyed
     */
    @Contract(pure = true)
    public boolean hasValidNet() {
        return this.net != null && this.net.isValid();
    }

    /**
     * Sets this node's network and marks the node {@link #isRecentlyChanged() recently changed}, so it
     * rechecks its neighbours on the next nodespace update.
     *
     * @param net the new network, or {@code null} to detach the node
     */
    public void setNet(@Nullable N net) {
        this.net = net;
        this.recentlyChanged = true;
    }

    /**
     * @param recentlyChanged whether this node rechecks its neighbours on the next nodespace update
     */
    public void setRecentlyChanged(boolean recentlyChanged) {
        this.recentlyChanged = recentlyChanged;
    }

    /**
     * @return {@code true} if this node rechecks its neighbours on the next nodespace update
     */
    @Contract(pure = true)
    public boolean isRecentlyChanged() {
        return recentlyChanged;
    }

    /**
     * Checks whether this node was removed from the nodespace. An expired node never joins a network
     * again; its owner must create and register a new one.
     *
     * @return {@code true} if this node is expired
     */
    @Contract(pure = true)
    public boolean isExpired() {
        return expired;
    }

    /**
     * @return a read-only view of this node's connections
     */
    @Contract(pure = true)
    public @NotNull @UnmodifiableView List<DirPos> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    /**
     * @return the network type this node belongs to
     */
    @Contract(pure = true)
    public @NotNull INetworkProvider<N> getProvider() {
        return provider;
    }

    /**
     * @return an immutable copy of every block this node occupies
     */
    @Contract(value = "-> new", pure = true)
    public @NotNull List<BlockPos> getPositions() {
        return List.copyOf(positions);
    }

    /**
     * @return the node's network
     */
    public @Nullable N getNet() {
        return net;
    }
}
