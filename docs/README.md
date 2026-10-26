# WoodLib (`at.woodexplosive.woodlib`)

Paper library (GUIs, ItemBuilder, custom/multiblocks, block entities, networks, scheduler) that you **shade into your plugin**. The plugin that ships WoodLib is the host: it calls `WoodLib.init(this)` / `WoodLib.disable()` – everything else only uses the API.

**Package:** `at.woodexplosive.woodlib` (API under `.api.*`, ready-made implementations under `.impl.*`)
**Dependency:** `com.github.EXPLOSIVEGAMER:WoodLib:v1.1` (JitPack)

---

## 1. Setup

```kotlin
// build.gradle.kts
repositories { maven("https://jitpack.io") }
dependencies { implementation("com.github.EXPLOSIVEGAMER:WoodLib:v1.1") }
```

- Shade WoodLib into your plugin JAR (e.g. with the Shadow plugin). Other plugins can use the WoodLib classes from your plugin by depending on it (`compileOnly` + a dependency entry in their `paper-plugin.yml` / `plugin.yml`).
- WoodLib ships its own **PacketEvents** and **CustomBlockData**, relocated to `at.woodexplosive.lib.packetevents` / `at.woodexplosive.lib.customblockdata`.

## 2. Lifecycle

| Call | Where | What happens |
|---|---|---|
| `WoodLib.init(host)` | `onEnable` | Load PacketEvents, `Scheduler.start()`, register listeners (GUI, CustomBlocks, Nodespace) + `/woodlib` command |
| `WoodLib.disable()` | `onDisable` | Stop scheduler, unload block entities, `CustomBlockRegistry.clear()`, terminate PacketEvents, clear Nodespace |
| `WoodLib.plugin()` / `WoodLib.logger()` | anywhere | Host plugin or SLF4J logger `woodlib:<Host>`; throw `IllegalStateException` before `init` |

> **Do not call `WoodLib.init(...)` twice** – a second call throws `IllegalStateException("WoodLib is already initialized")`. All tasks, listeners and `NamespacedKey`s (`gui_element_id`, `custom_block_id`) run under the host plugin's namespace.

## 3. Features

| Feature | File | Contents |
|---|---|---|
| GUIs | [gui.md](./gui.md) | `SimpleGui`, `SimplePagedGui`, `SimpleTabbedGui`, `SimpleTabbedPagedGui`, `GuiElement`, tabs, exit flags, events |
| ItemBuilder | [itembuilder.md](./itembuilder.md) | `ItemBuilder` / `AbstractItemBuilder`: name, lore, PDC, attributes, heads, item model, `linkCustomBlock` |
| Custom Blocks | [custom-blocks.md](./custom-blocks.md) | `CustomBlockBuilder` / `BaseCustomBlock`: multiblocks (HBM style), displays, ports, mining, drops, events |
| Block Entities | [block-entities.md](./block-entities.md) | `CustomBlockEntity`: tick, load/unload, persistent data, `CustomBlockEntities` lookup |
| Networks (UniNode) | [uninode.md](./uninode.md) | `UniNodespace`, `GenNode`, `NodeNet`, `INetworkProvider` – cable/pipe networks, debug particles |
| Scheduler & Utils | [scheduler.md](./scheduler.md) | `Scheduler` (ticks + real ms), `BlockPos`/`DirPos`, `MathUtil`, `/woodlib` command |

## 4. Important: Threading / Folia

WoodLib consistently uses the **global Bukkit scheduler** (`Bukkit.getScheduler()`) and `ServerTickStartEvent` – GUI ticks, block entities, mining, placement preview and the Nodespace all run on the main thread.

- On **Paper** this is fine.
- On **Folia**, `Bukkit.getScheduler()` throws an `UnsupportedOperationException` – WoodLib **does not run** there (as of v1.1). Do not declare `folia-supported: true` in a plugin that relies on WoodLib, without knowing what you are doing!
