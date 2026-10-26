package at.woodexplosive.woodlib.api.uninode;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.utils.BlockPos;
import at.woodexplosive.woodlib.api.utils.DirPos;
import at.woodexplosive.woodlib.impl.interfaces.Internal;
import at.woodexplosive.woodlib.impl.utils.Tuple.Pair;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The global registry of all {@link GenNode}s and live {@link NodeNet}s ("Universal Nodespace").
 *
 * <p>Nodes are stored per world and looked up by position <b>and</b> {@link INetworkProvider}, so
 * nodes of different network types can share a block. A multiblock node is stored under each of its
 * positions. The nodespace is decoupled from the blocks that create the nodes, so networks survive
 * their chunks being unloaded; it is volatile though and must be rebuilt from the world after a
 * restart.</p>
 *
 * <p>{@link #updateNodespace()} runs once per server tick, before world and entity ticks: it connects
 * new or changed nodes into networks, then ticks every network.</p>
 *
 * <p>Everything here runs on the server main thread and is not synchronized.</p>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class UniNodespace {
    /** Nodes per world, keyed by the world's UID. */
    private static final Map<UUID, UniNodeWorld> worlds = new HashMap<>();
    /** Every live network. Networks add themselves in their constructor and remove themselves when invalidated. */
    static final Set<NodeNet<?, ?, ?>> activeNodeNets = new HashSet<>();

    /**
     * Looks up the node of the given network type at a position.
     *
     * @param world the world to look in
     * @param x block x
     * @param y block y
     * @param z block z
     * @param type the network type
     * @return the node, or {@code null} if there is none
     */
    @Contract(pure = true)
    public static <N extends NodeNet<?, ?, ?>> @Nullable GenNode<N> getNode(@NotNull World world, int x, int y, int z, @NotNull INetworkProvider<N> type) {
        UniNodeWorld nodeWorld = worlds.get(world.getUID());
        if (nodeWorld != null)
            return (GenNode<N>) nodeWorld.nodes.get(new Pair<BlockPos, INetworkProvider<?>>(new BlockPos(x, y, z), type));
        return null;
    }

    /**
     * Looks up the node of the given network type at a position.
     *
     * @param world the world to look in
     * @param pos the position
     * @param type the network type
     * @return the node, or {@code null} if there is none
     */
    @Contract(pure = true)
    public static <N extends NodeNet<?, ?, ?>> @Nullable GenNode<N> getNode(@NotNull World world, @NotNull BlockPos pos, @NotNull INetworkProvider<N> type) {
        return getNode(world, pos.getX(), pos.getY(), pos.getZ(), type);
    }

    /**
     * Registers a node under each of its positions. The node connects to its neighbours on the next
     * {@link #updateNodespace()}; until then it has no network.
     *
     * <p>Look for a surviving node with {@link #getNode} first (it may have outlived a chunk unload)
     * and only create a new one if there is none or it is {@link GenNode#isExpired() expired}. An
     * existing node at the same position and type is silently replaced.</p>
     *
     * @param world the world the node lives in
     * @param node the node to register
     */
    public static void createNode(@NotNull World world, @NotNull GenNode<?> node) {
        UniNodeWorld nodeWorld = worlds.get(world.getUID());
        if (nodeWorld == null) {
            nodeWorld = new UniNodeWorld();
            worlds.put(world.getUID(), nodeWorld);
        }
        nodeWorld.pushNodes(node);
    }

    /**
     * Removes the node of the given network type at a position: destroys its whole network, removes
     * it from every position it occupies and marks it {@link GenNode#isExpired() expired}. The other
     * nodes of the destroyed network reconnect into the correct sub-networks over the next tick or two.
     * Does nothing if there is no such node.
     *
     * <p>Call this when the block is broken, not when its chunk unloads.</p>
     *
     * @param world the world to look in
     * @param x block x
     * @param y block y
     * @param z block z
     * @param type the network type
     */
    public static void destroyNode(@NotNull World world, int x, int y, int z, @NotNull INetworkProvider<?> type) {
        GenNode<?> node = getNode(world, x, y, z, type);
        if (node != null) {
            worlds.get(world.getUID()).popNode(node);
        }
    }

    /**
     * Removes the node of the given network type at a position. See
     * {@link #destroyNode(World, int, int, int, INetworkProvider)}.
     *
     * @param world the world to look in
     * @param pos the position
     * @param type the network type
     */
    public static void destroyNode(@NotNull World world, @NotNull BlockPos pos, @NotNull INetworkProvider<?> type) {
        destroyNode(world, pos.getX(), pos.getY(), pos.getZ(), type);
    }

    /**
     * Removes the given node. See {@link #destroyNode(World, int, int, int, INetworkProvider)}.
     *
     * @param world the world the node lives in
     * @param node the node to remove; does nothing if {@code null}
     */
    public static void destroyNode(@NotNull World world, @Nullable GenNode<?> node) {
        if (node != null) {
            UniNodeWorld nodeWorld = worlds.get(world.getUID());
            if (nodeWorld == null) return;
            nodeWorld.popNode(node);
        }
    }

    /** Ticks until the next cleanup of expired nodes and empty networks. */
    private static int reapTimer = 0;

    /**
     * Runs one nodespace tick. Called by WoodLib at the start of every server tick; do not call it
     * yourself.
     * <ol>
     *     <li>Every node without a valid network or marked {@link GenNode#isRecentlyChanged() recently
     *     changed} connects to its neighbours; if it still has no network, it gets a new one.</li>
     *     <li>{@link NodeNet#resetTrackers()} runs on every network, then {@link NodeNet#update()}.</li>
     *     <li>Every 5 minutes, expired nodes are dropped from their networks and empty networks are
     *     discarded.</li>
     * </ol>
     */
    public static void updateNodespace() {
        for (UniNodeWorld nodeWorld : worlds.values()) {

            for (Map.Entry<Pair<BlockPos, INetworkProvider<?>>, GenNode<?>> entry : nodeWorld.nodes.entrySet()) {
                GenNode<?> node = entry.getValue();
                INetworkProvider<?> provider = entry.getKey().value();
                if (!node.hasValidNet() || node.recentlyChanged) {
                    checkNodeConnection(nodeWorld, node, provider);
                    node.recentlyChanged = false;
                }
            }
        }

        updateNetworks();
        updateReapTimer();
    }

    /**
     * Resets and updates every network, then runs the periodic cleanup if it is due. An exception in
     * one network's {@link NodeNet#update()} is logged and does not stop the others.
     */
    private static void updateNetworks() {
        for (NodeNet<?, ?, ?> net : activeNodeNets) net.resetTrackers();
        for (NodeNet<?, ?, ?> net : activeNodeNets) {
            try {
                net.update();
            } catch (Exception e) {
                WoodLib.logger().error("Error while updating a Network", e);
            }
        }

        if (reapTimer <= 0) {
            activeNodeNets.forEach(net -> net.links.removeIf(link -> link.expired));
            activeNodeNets.removeIf(net -> {
                if (!net.links.isEmpty()) return false;
                net.valid = false;
                return true;
            });
        }
    }

    /** Counts the cleanup timer down and restarts it at 5 minutes once it ran out. */
    private static void updateReapTimer() {
        if (reapTimer <= 0) reapTimer = 5 * 60 * 20; // 5 minutes
        else reapTimer--;
    }

    /**
     * Connects a node to every neighbour that confirms the connection, merging networks as needed. If
     * the node still has no valid network afterwards, it gets a new single-node network from
     * {@code provider}.
     *
     * @param world the world the node lives in
     * @param node the node to connect
     * @param provider the node's network type
     */
    private static void checkNodeConnection(@NotNull UniNodeWorld world, @NotNull GenNode<?> node, @NotNull INetworkProvider<?> provider) {
        for (DirPos con : node.connections) {
            GenNode<?> conNode = world.getNode(con, provider);
            if (conNode != null) {
                if (conNode.hasValidNet() && conNode.net == node.net) continue;
                if (checkConnection(conNode, con, false)) {
                    connectToNode(node, conNode);
                }
            }
        }

        if (node.net == null || !node.net.isValid()) ((NodeNet) provider.provideNetwork()).joinLink(node);
    }

    /**
     * Checks whether {@code connectsTo} confirms a connection coming from {@code connectFrom}: it needs
     * a connection of its own that starts at {@code connectFrom}'s target position and points in the
     * opposite direction, i.e. back at the connecting node.
     *
     * <p>A {@link org.bukkit.block.BlockFace#SELF SELF} connection has no offset and is its own
     * opposite, so two {@code SELF} connections pointing at each other's positions connect regardless
     * of distance (long-range links).</p>
     *
     * @param connectsTo the neighbour node being connected to
     * @param connectFrom the connecting node's connection pointing at {@code connectsTo}
     * @param skipSideCheck {@code true} to accept the reverse connection regardless of its direction
     * @return {@code true} if the connection is confirmed from both sides
     */
    @Contract(pure = true)
    public static boolean checkConnection(@NotNull GenNode<?> connectsTo, @NotNull DirPos connectFrom, boolean skipSideCheck) {
        for (DirPos revCon : connectsTo.connections) {
            if (revCon.getX() - revCon.getDir().getModX() == connectFrom.getX()
                    && revCon.getY() - revCon.getDir().getModY() == connectFrom.getY()
                    && revCon.getZ() - revCon.getDir().getModZ() == connectFrom.getZ()
                    && (revCon.getDir() == connectFrom.getDir().getOppositeFace()
                    || skipSideCheck)) {

                return true;
            }
        }
        return false;
    }

    /**
     * Joins two confirmed neighbours into one network:
     * <ul>
     *     <li>both have a network: the larger one absorbs the smaller one,</li>
     *     <li>only one has a network: the other node joins it,</li>
     *     <li>neither has one: nothing happens; the caller creates a new network afterwards.</li>
     * </ul>
     *
     * @param origin the node being connected
     * @param connection its neighbour
     */
    private static void connectToNode(@NotNull GenNode<?> origin, @NotNull GenNode<?> connection) {
        if (origin.hasValidNet() && connection.hasValidNet()) {
            if (origin.net.links.size() > connection.net.links.size()) {
                origin.net.joinNetworks(connection.net);
            } else {
                connection.net.joinNetworks(origin.net);
            }
        } else if (!origin.hasValidNet() && connection.hasValidNet()) {
            ((NodeNet) connection.net).joinLink(origin);
        } else if (origin.hasValidNet()) {
            ((NodeNet) origin.net).joinLink(connection);
        }
    }

    /**
     * Drops all nodes of a world: destroys their networks and marks every node
     * {@link GenNode#isExpired() expired}. Called by WoodLib when a world unloads. Does nothing if the
     * world has no nodes.
     *
     * @param world the unloading world
     */
    public static void removeWorld(@NotNull World world) {
        UniNodeWorld nodeWorld = worlds.remove(world.getUID());
        if (nodeWorld == null) return;
        for (GenNode<?> node : nodeWorld.nodes.values()) {
            if (node.hasValidNet()) {
                node.net.destroy();
            }
            node.expired = true;
        }
    }

    /**
     * Internal: called by {@code WoodLib.disable()}. Expires every node, invalidates every network and
     * resets the nodespace to its initial state.
     */
    @Internal
    public static void clearNodes() {
        for (UniNodeWorld nodeWorld : worlds.values()) nodeWorld.nodes.values().forEach(node -> node.expired = true);

        worlds.clear();
        activeNodeNets.forEach(net -> net.valid = false);
        activeNodeNets.clear();
        reapTimer = 0;
    }

    /** The nodes of a single world, keyed by position and network type. */
    static class UniNodeWorld {
        /** Every node, stored once under each of its positions. */
        public HashMap<Pair<BlockPos, INetworkProvider<?>>, GenNode<?>> nodes = new LinkedHashMap<>();

        /**
         * @param pos the position; a {@link DirPos} works too, only the coordinates are compared
         * @param provider the network type
         * @return the node, or {@code null} if there is none
         */
        @Contract(pure = true)
        public <N extends NodeNet<?, ?, ?>> @Nullable GenNode<N> getNode(@NotNull BlockPos pos, @NotNull INetworkProvider<N> provider) {
            return (GenNode<N>) nodes.get(new Pair<BlockPos, INetworkProvider<?>>(pos, provider));
        }

        /**
         * Stores a node under each of its positions.
         *
         * @param node the node to store
         */
        public void pushNodes(@NotNull GenNode<?> node) {
            for (BlockPos pos : node.positions) {
                nodes.put(new Pair<>(pos, node.provider), node);
            }
        }

        /**
         * Destroys a node's network, removes the node from all its positions and marks it expired.
         *
         * @param node the node to remove
         */
        public void popNode(@NotNull GenNode<?> node) {
            if (node.hasValidNet()) node.net.destroy();
            for (BlockPos pos : node.positions) {
                nodes.remove(new Pair<BlockPos, INetworkProvider<?>>(pos, node.provider));
            }
            node.expired = true;
        }
    }
}
