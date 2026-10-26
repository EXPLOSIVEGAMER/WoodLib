# Networks / UniNode (`UniNodespace` / `GenNode` / `NodeNet`)

Generic conduit network system modeled after HBM-NTM ("Universal Nodespace"): conductors (cables, pipes …) register **nodes** that automatically connect into **networks**; machines subscribe as receivers/providers. Networks live independently of the blocks and survive chunk unloads.

**Package:** `at.woodexplosive.woodlib.api.uninode.*`, positions in `at.woodexplosive.woodlib.api.utils` (`BlockPos`, `DirPos`)

---

## 1. Building blocks

| Class | Role |
|---|---|
| `INetworkProvider<N>` | Network type + factory (`provideNetwork()`, `provideNode(positions...)`). Compared **by reference** → keep it as a singleton |
| `GenNode<N>` | Presence of a conductor at one or more positions + its `connections` (`DirPos`) |
| `NodeNet<R, P, L>` | A network: nodes (`L`), receivers (`R`), providers (`P`); `update()` does the actual work each tick |
| `UniNodespace` | Global registry per world (key = position **+** provider – different network types can share a block) |
| `ILoadedEndpoint` | `isLoaded()` – endpoints returning `false` are removed automatically (e.g. `CustomBlockEntity`) |

## 2. Defining a network type

```java
public final class EnergyNet extends NodeNet<EnergyReceiver, EnergyProvider, GenNode<EnergyNet>> {
    public static final long TIMEOUT = 3_000; // ms

    public static final INetworkProvider<EnergyNet> TYPE = new INetworkProvider<>() {
        @Override public EnergyNet provideNetwork() { return new EnergyNet(); }
        @Override public GenNode<EnergyNet> provideNode(BlockPos... positions) { return new GenNode<>(this, positions); }
    };

    @Override public void update() {
        long now = System.currentTimeMillis();
        receiverEntries.entrySet().removeIf(e -> now - e.getValue() > TIMEOUT || isBadLink(e.getKey()));
        providerEntries.entrySet().removeIf(e -> now - e.getValue() > TIMEOUT || isBadLink(e.getKey()));

        long supply = 0;
        for (EnergyProvider p : providerEntries.keySet()) supply += p.extract();
        for (EnergyReceiver r : receiverEntries.keySet()) supply = r.receive(supply);
    }
}
```

- The `NodeNet` constructor registers the network in the Nodespace itself.
- **`update()` must remove expired endpoints itself** (pattern above). `isBadLink` = an `ILoadedEndpoint` that is no longer loaded; can be overridden.
- `resetTrackers()` runs on all networks before any `update()` runs (for per-tick counters).
- **Never create/destroy networks or nodes inside `update()`** → `ConcurrentModificationException`.

## 3. Registering conductors (cables)

```java
// Cable placed / chunk loaded:
BlockPos pos = BlockPos.of(block);
GenNode<EnergyNet> node = UniNodespace.getNode(world, pos, EnergyNet.TYPE);
if (node == null || node.isExpired()) {               // reuse a surviving node
    node = EnergyNet.TYPE.provideNode(pos).setStandardConnections(pos); // all 6 sides
    UniNodespace.createNode(world, node);
}

// Cable broken (NOT on chunk unload!):
UniNodespace.destroyNode(world, pos, EnergyNet.TYPE);
```

- Only set connections **before** `createNode` (`setConnections`, `setStandardConnections`, `addConnection`). Changing the shape = destroy the node + create it again.
- A connection is only formed if the neighbor **points back** (same boundary, opposite direction). Two `BlockFace.SELF` connections pointing at each other connect over any distance (long-distance lines).
- A new node has **no network** until the next tick → always check `node.hasValidNet()` before access.
- Networks are never split: when a node is removed, the entire network is destroyed and the remaining nodes reconnect within 1–2 ticks. **All** receivers/providers are lost in the process → endpoints must re-subscribe regularly.
- The Nodespace is transient: after a restart, conductors must re-register their nodes on chunk load.

## 4. Subscribing machines (receivers / providers)

Typically from a [block entity](./block-entities.md) with ports, e.g. every 20 ticks:

```java
private void trySubscribe() {
    for (DirPos con : instance().portConnections()) {   // position in front of the port + direction
        GenNode<EnergyNet> node = UniNodespace.getNode(world(), con, EnergyNet.TYPE);
        boolean connected = node != null && node.hasValidNet();
        if (connected) node.getNet().addReceiver(this);   // refresh timestamp

        if (UniNodespace.isParticleDebug())
            EnergyNet.TYPE.spawnDebugParticles(world(), con, true, connected);
    }
}
```

| `NodeNet` method | Description |
|---|---|
| `addReceiver(r)` / `removeReceiver(r)` / `isSubscribed(r)` | Subscribe/unsubscribe a receiver (key in a `HashMap` → keep `equals`/`hashCode` stable) |
| `addProvider(p)` / `removeProvider(p)` / `isProvider(p)` | Subscribe/unsubscribe a provider |
| `getReceivers()` / `getProviders()` / `getLinks()` | Read-only views (endpoint → last subscription in ms) |
| `isValid()` / `destroy()` / `invalidate()` | Status / tear down completely / only unregister |

## 5. Per-tick flow

`UniNodespace.updateNodespace()` runs automatically on `ServerTickStartEvent` (do not call it yourself):

1. Nodes without a valid network or with `recentlyChanged` check their neighbors; the larger network absorbs the smaller one. Without a partner → new single-node network.
2. `resetTrackers()` on all networks, then `update()` (an exception in one network is logged, the others keep running).
3. Every 5 minutes: clean up expired nodes and empty networks.

World unload → `removeWorld` (all nodes of the world expire). `WoodLib.disable()` → `clearNodes()`.

## 6. Debug

`/woodlib netdebug [on|off]` (permission `woodlib.debug`, no argument = toggle) or `UniNodespace.setParticleDebug(bool)`. Default particles from `INetworkProvider.spawnDebugParticles`: slow sparks = connected, fast smoke = no network. Can be overridden for a custom look.
