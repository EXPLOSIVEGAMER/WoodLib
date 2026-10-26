# Custom Blocks & Multiblocks (`CustomBlockBuilder` / `BaseCustomBlock`)

"Fake" blocks modeled after HBM-NTM (`BlockDummyable`): invisible collision blocks (default `BARRIER`) around a **core**, rendered via block/item displays. Single block or multiblock up to 2000 cells, rotatable, with ports, mining time following the vanilla formula, drops/loot tables and a placement preview.

**Package:** `at.woodexplosive.woodlib.api.block.*` (API, events), `at.woodexplosive.woodlib.impl.block.*` (`BaseCustomBlock`, `builder.CustomBlockBuilder`)

---

## 1. Quick start (single block)

```java
NamespacedKey id = new NamespacedKey(plugin, "slime_crate");

ICustomBlock crate = CustomBlockBuilder.of(id)
        .display(DisplayDefinition.builder(new ItemStack(Material.SLIME_BLOCK)) // ItemDisplay
                .translation(0.5f, 0.5f, 0.5f)                                   // cell center
                .build())
        .hardness(2.5f)                          // 0 = break instantly (default)
        .requiredToolType(ToolType.AXES)
        .setOnBlockInteract(event -> event.getPlayer().sendMessage(Component.text("Crate!")))
        .build();

ItemStack placer = ItemBuilder.of(Material.SLIME_BLOCK)
        .displayName("<green>Slime Crate")
        .linkCustomBlock(crate)                  // right-clicking with the item places the block
        .build();

CustomBlockRegistry.register(crate, placer);   // drop item when broken
```

Registration already works in `onLoad()` (pure in-memory map, independent of `WoodLib.init`). The identity of placed blocks is stored in the block/entity PDC (CustomBlockData) – clearing/refilling the registry does not affect already placed blocks.

---

## 2. Coordinates & shape

| Type | Meaning |
|---|---|
| `LocalPos(right, up, forward)` | Cell relative to the core (`LocalPos.CORE = 0,0,0`) in the structure's frame. Authored for facing **NORTH**: `x = right, y = up, z = -forward` |
| `LocalDirection` | `UP`, `DOWN`, `FORWARD`, `BACKWARD`, `LEFT`, `RIGHT` (from the structure's point of view), `opposite()`, `step()` |
| `Dimensions(up, down, forward, backward, left, right)` | Cells **in addition** to the core per direction; `Dimensions.NONE` = 1 block, `Dimensions.of(int[6])` in HBM format |
| `MultiblockShape` | Boxes + ports, `cells()`, `bounds()`, `MAX_CELLS = 2000`, `SINGLE` |
| `PortDefinition(pos, direction)` | Marked cell that connects outward (cables/pipes, see [uninode.md](./uninode.md)) |

```java
// 3x3x3 machine standing on its core row, with an energy port at the back center
ICustomBlock press = CustomBlockBuilder.of(new NamespacedKey(plugin, "press"))
        .dimensions(2, 0, 1, 1, 1, 1)                          // up, down, forward, backward, left, right
        .box(new Dimensions(0, 0, 0, 0, 0, 0), new LocalPos(0, 3, 0)) // extra box (chimney)
        .port(new LocalPos(0, 0, -1), LocalDirection.BACKWARD)
        .rotatable(true)                                       // front faces the player
        .collision(Material.BARRIER)
        .collision(new LocalPos(0, 0, 1), Material.IRON_TRAPDOOR.createBlockData()) // single cell
        .display(DisplayDefinition.builder(Material.PISTON.createBlockData())
                .translation(-1f, 0f, -1f).scale(3f, 3f, 3f).build())
        .entity(PressEntity::new)                              // see block-entities.md
        .build();
```

`build()` throws an `IllegalArgumentException` if a port or a collision override lies outside the shape, or if the shape has > 2000 cells.

---

## 3. Builder methods (`CustomBlockBuilder`)

| Method | Description |
|---|---|
| `of(NamespacedKey)` | Entry point with a unique ID (e.g. `myplugin:press`) |
| `dimensions(u, d, f, b, l, r)` / `box(Dimensions[, LocalPos offset])` | Box around the core or around `offset` (composite shapes). No box = single block |
| `port(LocalPos, LocalDirection)` | Add a port |
| `offset(int)` | How far the core is pushed away from the player when placing on floor/ceiling (default: forward extent) |
| `heightOffset(int)` | Additional vertical offset (default 0) |
| `display(DisplayDefinition)` / `displays(List)` / `single(List)` | Displays at the core |
| `collision(BlockData / Material)` | Collision block for all cells (default `BARRIER`) |
| `collision(LocalPos, BlockData)` | Collision block for one cell (rotated along, e.g. stairs) |
| `rotatable(bool)` | `true` = front faces the player; `false` (default) = always NORTH |
| `hardness(float)` | Mining hardness like vanilla; `<= 0` = instant break |
| `requiredToolType(Tag<Material>)` | "Correct tool", e.g. `ToolType.PICKAXES` (`null` = hand is enough) |
| `minimumToolTier(ToolTier)` | Minimum tier (`WOOD`, `GOLD`, `COPPER`, `STONE`, `IRON`, `DIAMOND`, `NETHERITE`) |
| `blockTags(List<Tag<Material>>)` | Pure metadata (`hasBlockTag`), **no** vanilla behavior |
| `setOnBlockInteract` / `setOnBlockPlaceEvent` / `setOnBlockBreakEvent` | Hooks (run **before** the Bukkit event and can cancel it) |
| `entity(Function<MultiblockInstance, CustomBlockEntity>)` | Factory for the runtime instance ([block-entities.md](./block-entities.md)) |
| `build()` | Creates the `ICustomBlock` – **does not register it** |

### As a class (`BaseCustomBlock`)

```java
public class PressBlock extends BaseCustomBlock {
    @Override public @NotNull NamespacedKey id() { return new NamespacedKey(plugin, "press"); }
    @Override public @NotNull Dimensions getDimensions() { return new Dimensions(2, 0, 1, 1, 1, 1); }
    @Override public int getOffset() { return 1; }
    @Override public @NotNull List<PortDefinition> getPorts() {
        return List.of(new PortDefinition(new LocalPos(0, 0, -1), LocalDirection.BACKWARD));
    }
    @Override public boolean rotatable() { return true; }
    @Override public @NotNull BlockFace modifyFacing(@NotNull BlockFace f) { // only 2 orientations
        return f == BlockFace.SOUTH ? BlockFace.NORTH : f == BlockFace.WEST ? BlockFace.EAST : f;
    }
    @Override public boolean canPlace(@NotNull PlacementContext ctx) {
        return ctx.player().hasPermission("myplugin.press") && super.canPlace(ctx);
    }
}
```

`shape()` is built on first access from `getAllDimensions()` (default: `getDimensions()` around the core) + `getPorts()` and cached – the values must be constant. All `ICustomBlock` defaults can be overridden: `displays()`, `collisionData(LocalPos)`, `canPlace`, `showPlacementPreview()`, `createEntity`, `onInteract/onPlace/onBreak`, `hardness()` …

---

## 4. Displays (`DisplayDefinition`)

| Entry point | Renders as |
|---|---|
| `DisplayDefinition.builder(BlockData)` | `BlockDisplay` (vanilla block model) |
| `DisplayDefinition.builder(ItemStack)` | `ItemDisplay` – for custom resource pack models (`setItemModel`), additionally `itemTransform(...)` (default `FIXED`) |

Common options: `translation(x, y, z)`, `scale(...)`, `leftRotation/rightRotation(Quaternionf)`, `billboard(...)` (default `FIXED`), `brightness(...)`, `glowing(Color)`. Translation is relative to the **corner** of the core cell, authored for NORTH, and rotated with the structure.

---

## 5. Placing, breaking, drops

- **Placing:** Right-click with a linked item (`linkCustomBlock`). The structure grows away from the player (`offset`), flush with the clicked face on walls/ceilings. `canPlace` (default: all cells replaceable + no LivingEntities) → `onPlace` hook → `CustomBlockPlaceEvent`. Outside of Creative the item is reduced by 1.
- **Placement preview:** While a linked item is held, WoodLib shows the cells via particles (only for that player): green = placeable, red = not, ports blue. Can be disabled with `showPlacementPreview() = false`.
- **Breaking:** Left-clicking **any** cell breaks the whole structure. With `hardness > 0`, mining time is handled server-side via PacketEvents (`PLAYER_DIGGING`) using the vanilla formula (tool, efficiency, effects); Creative breaks instantly.
- **Drops:** Only outside of Creative **and** with the correct tool. A registered loot table takes precedence over the drop item. The tool takes durability damage like in vanilla.
- **Region protection:** The listener runs at `HIGH` – anything WorldGuard & co. cancel beforehand is ignored.

### Registry (`CustomBlockRegistry`)

| Method | Description |
|---|---|
| `register(block, dropItem)` | Register block + drop item |
| `registerLootTable(blockId, lootTableKey)` | Datapack/vanilla loot table instead of a drop item (resolved fresh on every break → `/reload` of datapacks takes effect) |
| `unregister(id)` / `get(id)` / `isRegistered(id)` / `all()` / `dropItem(id)` / `lootTable(id)` | Management |
| `clear()` | Called by `WoodLib.disable()` |

---

## 6. Events

All extend `CustomBlockEvent`: `getPlayer()`, `getInstance()` (`MultiblockInstance`), `getCustomBlock()`, `getCoreBlock()`, `getFacing()`, `getWorld()`, `getEntity()` (always `null` on place).

| Event | Additionally | Cancelling causes |
|---|---|---|
| `CustomBlockPlaceEvent` | `getPlacement()`, `getClickedBlock()`, `getClickedFace()`, `getItemInHand()` | Not placed, item is kept |
| `CustomBlockBreakEvent` | `getClickedBlock()`, `isCoreClicked()`, `isPortClicked()`, `isDropping()` | Structure stays completely intact |
| `CustomBlockInteractEvent` | `getClickedBlock()`, `isCoreClicked()`, `isPortClicked()`, `getHand()`, `getItemInHand()` | Nothing – pure observer event; WoodLib has no default action (e.g. opening a GUI is your job) |

Interact only fires for the main hand and only if **no** linked item is held – otherwise placing is attempted. The vanilla `PlayerInteractEvent` on a custom block cell is always cancelled.

---

## 7. `MultiblockInstance`

Placed structure = definition + core block + facing. Useful in events and block entities:

| Method | Description |
|---|---|
| `core()` / `facing()` / `customBlock()` | Basic data (`facing` is always NORTH/EAST/SOUTH/WEST) |
| `toWorld(LocalPos)` / `toWorld(LocalDirection)` | Local coordinate → world block or `BlockFace` |
| `cells()` / `portBlocks()` / `portConnections()` | All cells, port blocks, port connections as `DirPos` (for `GenNode`) |
| `isCore(block)` / `isPort(block)` | Checks |
| `data()` | Persistent `CustomBlockData` of the core (type: `at.woodexplosive.lib.customblockdata.CustomBlockData`) – use your own keys |
