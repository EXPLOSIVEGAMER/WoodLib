package at.woodexplosive.woodlib.api.uninode;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * A network of connected {@link GenNode}s plus the endpoints (machines) subscribed to it. Subclasses
 * implement {@link #update()} to do the actual network work (distributing energy, moving fluids, ...)
 * once per server tick.
 *
 * <p>A network registers itself with the {@code UniNodespace} in its constructor and unregisters in
 * {@link #invalidate()}. <b>Never create or destroy networks or nodes inside {@link #update()}</b>: the
 * nodespace is iterating over all networks at that point, which would throw a
 * {@link ConcurrentModificationException}.</p>
 *
 * <p>Networks are never split. When a node is removed, its whole network is {@link #destroy() destroyed}
 * and the remaining nodes reconnect into the correct sub-networks over the next tick or two. All
 * receivers and providers are lost in the process, which is why endpoints must re-subscribe
 * regularly and entries expire after a timeout.</p>
 *
 * <p>Everything runs on the server main thread and is not synchronized.</p>
 *
 * @param <R> the receiver (consumer) type
 * @param <P> the provider (supplier) type
 * @param <L> the node ("link") type making up this network
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class NodeNet<R, P, L extends GenNode> {
    /** {@code false} once this network was {@link #invalidate() invalidated}. */
    protected boolean valid = true;
    /** Every node belonging to this network. */
    protected final @NotNull Set<L> links = new LinkedHashSet<>();
    /** Subscribed receivers mapped to their last subscription time (epoch millis). */
    protected final @NotNull Map<R, Long> receiverEntries = new HashMap<>();
    /** Subscribed providers mapped to their last subscription time (epoch millis). */
    protected final @NotNull Map<P, Long> providerEntries = new HashMap<>();


    /**
     * Creates the network and registers it with the {@code UniNodespace}, so it ticks from the next
     * nodespace update on.
     */
    public NodeNet() {
        UniNodespace.activeNodeNets.add(this);
    }

    /**
     * @param receiver the receiver to check
     * @return {@code true} if {@code receiver} is currently subscribed to this network
     */
    @Contract(pure = true)
    public boolean isSubscribed(@NotNull R receiver) { return this.receiverEntries.containsKey(receiver); }

    /**
     * Subscribes a receiver or refreshes its subscription time. Receivers must call this regularly,
     * well below the network's timeout, since subscriptions expire and are lost whenever the network
     * is rebuilt.
     *
     * <p>The receiver is used as a {@link HashMap} key, so its {@code equals}/{@code hashCode} must
     * not change while it is subscribed.</p>
     *
     * @param receiver the receiver to subscribe
     */
    public void addReceiver(@NotNull R receiver) { this.receiverEntries.put(receiver, System.currentTimeMillis()); }

    /**
     * @param receiver the receiver to unsubscribe; does nothing if it isn't subscribed
     */
    public void removeReceiver(@NotNull R receiver) { this.receiverEntries.remove(receiver); }

    /**
     * @param provider the provider to check
     * @return {@code true} if {@code provider} is currently subscribed to this network
     */
    @Contract(pure = true)
    public boolean isProvider(@NotNull P provider) { return this.providerEntries.containsKey(provider); }

    /**
     * Subscribes a provider or refreshes its subscription time. The same rules as for
     * {@link #addReceiver} apply.
     *
     * @param provider the provider to subscribe
     */
    public void addProvider(@NotNull P provider) { this.providerEntries.put(provider, System.currentTimeMillis()); }

    /**
     * @param provider the provider to unsubscribe; does nothing if it isn't subscribed
     */
    public void removeProvider(@NotNull P provider) { this.providerEntries.remove(provider); }

    /**
     * Merges {@code network} into this one: takes over all its nodes, receivers and providers (keeping
     * their original subscription times), then destroys it. Called by the nodespace with the larger
     * network absorbing the smaller one.
     *
     * @param network the network to absorb; does nothing if it is this network
     */
    public void joinNetworks(@NotNull NodeNet network) {
        if (network == this) return;

        List<L> oldNodes = new ArrayList<>(network.links.size());
        oldNodes.addAll(network.links);

        for (L link : oldNodes) forceJoinLink(link);
        network.links.clear();

        this.receiverEntries.putAll(network.receiverEntries);
        this.providerEntries.putAll(network.providerEntries);
        network.destroy();
    }

    /**
     * Adds a node to this network, removing it from its previous network first.
     *
     * @param node the node to add
     * @return this network, for chaining
     */
    @Contract("_ -> this")
    public @NotNull NodeNet joinLink(@NotNull L node) {
        if (node.net != null) node.net.leaveLink(node);
        return forceJoinLink(node);
    }

    /**
     * Adds a node to this network <b>without</b> removing it from its previous network. Only use this
     * for nodes that have no network or whose old network is discarded anyway (as in
     * {@link #joinNetworks}); otherwise use {@link #joinLink}.
     *
     * @param node the node to add
     * @return this network, for chaining
     */
    @Contract("_ -> this")
    public @NotNull NodeNet<R, P, L> forceJoinLink(@NotNull L node) {
        this.links.add(node);
        node.setNet(this);
        return this;
    }

    /**
     * Removes a node from this network and detaches it ({@code net = null}).
     *
     * @param node the node to remove
     */
    public void leaveLink(@NotNull L node) {
        node.setNet(null);
        this.links.remove(node);
    }

    /**
     * @return a read-only view of every node in this network
     */
    @Contract(pure = true)
    public @NotNull @UnmodifiableView Set<L> getLinks() {
        return Collections.unmodifiableSet(links);
    }

    /**
     * @return a read-only view of every receiver in this network
     */
    @Contract(pure = true)
    public @NotNull @UnmodifiableView Map<R, Long> getReceivers() { return Collections.unmodifiableMap(receiverEntries); }

    /**
     * @return a read-only view of every provider in this network
     */
    @Contract(pure = true)
    public @NotNull @UnmodifiableView Map<P, Long> getProviders() { return Collections.unmodifiableMap(providerEntries); }

    /**
     * Marks this network as dead and unregisters it from the {@code UniNodespace}. Does not touch its
     * nodes; use {@link #destroy()} to fully tear the network down.
     */
    public void invalidate() {
        valid = false;
        UniNodespace.activeNodeNets.remove(this);
    }

    /**
     * @return {@code false} once this network was invalidated or destroyed
     */
    @Contract(pure = true)
    public boolean isValid() {
        return valid;
    }

    /**
     * Resets per-tick counters. Called on <b>every</b> network before any network's {@link #update()}
     * runs. Does nothing by default.
     */
    public void resetTrackers() {}

    /**
     * Does this network's work for the current tick. Called once per server tick, after
     * {@link #resetTrackers()}, in no particular order relative to other networks.
     *
     * <p>Implementations must drop expired endpoints themselves, typically first thing:</p>
     * <pre>{@code
     * long now = System.currentTimeMillis();
     * receiverEntries.entrySet().removeIf(e -> now - e.getValue() > TIMEOUT || isBadLink(e.getKey()));
     * providerEntries.entrySet().removeIf(e -> now - e.getValue() > TIMEOUT || isBadLink(e.getKey()));
     * }</pre>
     *
     * <p>Never create or destroy networks or nodes here.</p>
     */
    public abstract void update();

    /**
     * Tears this network down: {@link #invalidate() invalidates} it, detaches every node still pointing
     * at it, and drops all nodes, receivers and providers. The detached nodes reconnect on the next
     * nodespace update.
     */
    public void destroy() {
        this.invalidate();
        for (L link : links) if (link.net == this) link.setNet(null);
        links.clear();
        receiverEntries.clear();
        providerEntries.clear();
    }

    /**
     * Checks whether an endpoint should be dropped regardless of its subscription time. By default
     * this is the case for an {@link ILoadedEndpoint} that is no longer loaded. Override to add
     * network-specific criteria.
     *
     * @param obj the receiver or provider to check
     * @return {@code true} if the endpoint should be removed
     */
    @Contract(pure = true)
    protected boolean isBadLink(@NotNull Object obj) {
        return obj instanceof ILoadedEndpoint e && !e.isLoaded();
    }
}
