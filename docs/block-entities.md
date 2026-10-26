# Block Entities (`CustomBlockEntity` / `CustomBlockEntities`)

Runtime object per placed [custom block](./custom-blocks.md) – the counterpart to a TileEntity: ticks every tick while the core chunk is loaded and can hold state (e.g. energy storage, network receiver). Fully managed by WoodLib.

**Package:** `at.woodexplosive.woodlib.api.block.CustomBlockEntity`, `CustomBlockEntities`

---

## 1. Quick start

```java
public class PressEntity extends CustomBlockEntity {
    private static final NamespacedKey ENERGY = new NamespacedKey(plugin, "energy");
    private long energy;

    public PressEntity(MultiblockInstance instance) { super(instance); }

    @Override public void onLoad() {                // after placing or chunk load
        energy = data().getOrDefault(ENERGY, PersistentDataType.LONG, 0L);
    }

    @Override public void tick() {                  // every server tick
        if (energy >= 100) { energy -= 100; /* produce */ }
    }

    @Override public void onUnload() {              // chunk/world unload or shutdown
        data().set(ENERGY, PersistentDataType.LONG, energy);
    }

    @Override public void onRemove() {              // structure broken (data() still readable)
        // e.g. unsubscribe from networks
    }
}

ICustomBlock press = CustomBlockBuilder.of(id)
        .dimensions(2, 0, 1, 1, 1, 1)
        .entity(PressEntity::new)                   // or override createEntity(...) in BaseCustomBlock
        .build();
```

---

## 2. Lifecycle

| Phase | Hook | When |
|---|---|---|
| Create | Factory / `createEntity(instance)` | On placement **and** every time the core chunk loads (new object!) |
| Load | `onLoad()` | Right after registration – read state from `data()` |
| Tick | `tick()` | Every tick while the core chunk is loaded. Do **not** assume neighboring chunks are loaded |
| Unload | `onUnload()` | Chunk/world unload or `WoodLib.disable()` – the structure still exists, save state |
| Remove | `onRemove()` | Structure broken, **before** the blocks are removed |

- The object is **transient**: anything that should survive a chunk unload or restart belongs in `data()` (PDC of the core, `at.woodexplosive.lib.customblockdata.CustomBlockData`). Use your own `NamespacedKey`s.
- On placement, `getEntity()` in the `CustomBlockPlaceEvent` is still `null` – the entity is only created afterwards.
- Everything runs on the main thread (Bukkit scheduler).

## 3. Access

| Method | Description |
|---|---|
| `instance()` / `core()` / `world()` / `facing()` | Structure, core block, world, orientation |
| `data()` | Persistent data of the core |
| `isLoaded()` | `false` after unload/remove – networks then automatically drop the entity (`ILoadedEndpoint`) |

```java
// From outside: any cell (core, dummy or port) → loaded entity
CustomBlockEntity any = CustomBlockEntities.get(clickedBlock);
PressEntity press = CustomBlockEntities.get(clickedBlock, PressEntity.class); // null if wrong type
CustomBlockEntity byInstance = CustomBlockEntities.get(event.getInstance());
```

## 4. With networks

`CustomBlockEntity` implements `ILoadedEndpoint` and is a stable key by object identity – ideal as a receiver/provider in a `NodeNet`. Typical pattern in `tick()`: look up the adjacent `GenNode`s via `instance().portConnections()` and re-subscribe regularly via `addReceiver(this)` / `addProvider(this)`. Details in [uninode.md](./uninode.md).
