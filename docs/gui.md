# WoodLib GUIs (`SimpleGui` / `SimplePagedGui` / `SimpleTabbedGui` / `SimpleTabbedPagedGui`)

Inventory GUIs whose `InventoryHolder` is the GUI itself – a single listener (`AbstractGui.GuiListener`, registered by `WoodLib.init`) routes all clicks/drags/opens/closes to the right GUI. Use a builder or a subclass, optionally multi-page and/or with tabs.

**Package:** `at.woodexplosive.woodlib.api.gui.*` (interfaces, events), `at.woodexplosive.woodlib.impl.gui.*` (implementations)

---

## 1. Quick start

```java
SimpleGui gui = SimpleGui.builder(Component.text("Menu"), 27, null) // parent = null
        .setPlayerManipulation(false) // default: items cannot be moved
        .build();

gui.setSlot(13, GuiElement.builder(Material.DIAMOND)
        .displayName("<aqua><bold>Reward")
        .stringLore("<gray>Click to claim")
        .setCallback(event -> {
            event.getWhoClicked().sendMessage(Component.text("Claimed!"));
            return true; // true = cancel the click
        })
        .buildElement());

gui.setSlot(26, GuiElement.decoElement(Material.BARRIER, "<red>Close")); // without callback
gui.setSlotCallback(26, event -> { event.getGui().close(); return true; });

gui.open(player);
```

Basic principle: builder → `build()` → fill slots → `open(player)`. All callbacks return a `boolean`: **`true` cancels** the underlying Bukkit event.

---

## 2. GUI types

| Class | Builder | Extras |
|---|---|---|
| `SimpleGui` | `builder(title, size, parent)` / `builder(title, InventoryType, parent)` | Single page |
| `SimplePagedGui` | same | `setPageSlots(...)`, `setOnPageChange(...)` – elements across multiple pages |
| `SimpleTabbedGui` | same | `setTabSlots(...)`, `setContentSlots(...)`, `addTab(...)`, `setOnTabChange(...)` |
| `SimpleTabbedPagedGui` | same | Tabs **and** pages; tabs are `PagedTab`s with their own element list |

`size` = multiple of 9 (ignored if an `InventoryType` is given). `parent` may be `null` – otherwise the parent GUI is automatically reopened on close (see §6).

### Builder options (all types)

| Method | Callback event | Description |
|---|---|---|
| `setOnOpen(cb)` | `GuiOpenEvent` | `true` → abort opening |
| `setOnClose(cb)` | `GuiCloseEvent` | On close (return value ignored), see note in §6 |
| `setOnClickGlobal(cb)` | `GuiClickEvent` | Runs on **every** click before the slot callbacks |
| `setOnInteract(cb)` | `GuiInteractEvent` | Every interaction |
| `setOnDrag(cb)` | `GuiDragEvent` | Item drag across slots |
| `setOnTick(cb)` | `GuiTickEvent` | **Every tick** while the GUI is open (live updates) |
| `setPlayerManipulation(bool)` | – | `true` = player may move items freely (default `false`) |

---

## 3. Elements (`GuiElement` / `GuiElementBuilder`)

An `IGuiElement` = `ItemStack` + optional `ClickCallback`. `GuiElementBuilder` inherits the complete [ItemBuilder API](./itembuilder.md).

| Entry point | Description |
|---|---|
| `GuiElement.builder(Material[, amount])` | Interactive element → `.setCallback(...)` → `.buildElement()` |
| `GuiElement.decoElement(Material[, amount][, name])` | Decoration without callback; `name` as `Component` or MiniMessage string |
| `GuiElement.decoElement(itemBuilder)` | Decoration from a finished `ItemBuilder` |
| `GuiElement.empty()` | Empty slot |
| `GuiElementBuilder.of(element / itemStack)` | Builder **without** copy (shares the stack) |
| `GuiElementBuilder.copyOf(element / itemStack / itemBuilder)` | Builder with a cloned stack |
| `element.copy()` | Independent copy (item + callback) |

- Elements with a callback get their UUID stamped into the PDC (`IGuiElement.idKey()` = `<host>:gui_element_id`). This lets the GUI find the callback even if the item was moved (shift-click, hotbar swap, `playerManipulation = true`).
- Click order: **global** → **element callback** → **slot callback** (`setSlotCallback`) → `GuiClickEvent` to Bukkit. Any `true` (or a cancelled event) cancels the click.

### Slot methods (`IGui`)

| Method | Description |
|---|---|
| `setSlot(index, element)` / `setSlots(Map)` | Put an element into a slot |
| `addSlot(element)` / `addSlots(...)` | First free slot (for paged/tabbed: first free **non**-page/tab slot) |
| `setSlotCallback(index, cb)` | Callback independent of the element |
| `open(player)` / `close()` / `redraw()` | Open / close for all viewers / rebuild |
| `getPlayer()` / `getTitle()` / `getInventory()` | Last player who opened it, title, Bukkit inventory |

---

## 4. Multi-page (`SimplePagedGui`)

```java
SimplePagedGui gui = SimplePagedGui.builder(Component.text("Shop"), 54, null)
        .setPageSlots(MathUtil.intRange(0, 45))        // slots 0–44 = page content (default: all slots)
        .setOnPageChange(event -> false)               // true = abort page change
        .build();

for (ShopItem item : items) gui.addPageElement(toElement(item));

gui.setPreviousPageElement(45, GuiElement.decoElement(Material.ARROW, "<gray>« Back"));
gui.setNextPageElement(53, GuiElement.decoElement(Material.ARROW, "<gray>Next »"));
gui.open(player);          // page 0
gui.open(player, 2);       // directly page 2
```

- `setNext/PreviousPageElement` automatically attach a page-turning callback **if** the element does not have its own.
- `nextPage()` / `previousPage()` / `setPage(int)` (clamped to `0 … getMaxPage()`) fire a `GuiPageChangeEvent` and rebuild the GUI via `redraw()`.
- Elements: `addPageElement(s)`, `removePageElement(s)`, `setPageElement(LinkedList)`, `getPageElements()`. Call `redraw()` after changes while the GUI is open.
- `GuiPageChangeEvent`: `getOldPage()`, `getNewPage()`, `getMaxPage()`, `hasChanged()`, `isStartPage()`, `isMaxPage()`, cancellable.
- `open(player)` always opens page **0**; for the current page use `open(player, gui.getPage())` or `redraw()`.

---

## 5. Tabs (`SimpleTabbedGui` / `SimpleTabbedPagedGui`)

```java
Tab general = Tab.of(GuiElement.decoElement(Material.BOOK, "<yellow>General"));
general.addContentElements(elementA, elementB);   // next free content slots
general.setContentElement(22, elementC);          // fixed slot

SimpleTabbedGui gui = SimpleTabbedGui.builder(Component.text("Settings"), 54, null)
        .setTabSlots(0, 1, 2, 3)                      // row of tab buttons
        .setContentSlots(MathUtil.intRange(9, 54))    // content of the active tab
        .addTab(general)
        .addTab(Tab.of(GuiElement.decoElement(Material.COMPASS, "<aqua>Teleport")))
        .setOnTabChange(event -> false)               // true = abort change
        .build();
gui.open(player);
```

- A tab is **not a separate GUI**, but a button element + a `slot → element` map. On `addTab` the tab adopts the GUI's content slots; the first tab becomes active.
- Tab buttons without their own callback automatically switch to their tab when clicked.
- `setTab(tab)`, `nextTab()`, `previousTab()`, `getTab()`, `getTabIndex()`, `open(player, tab)`.
- `GuiTabChangeEvent`: `getOldTab()`, `getNewTab()`, `hasChanged()`, cancellable.
- **Tabbed + paged:** `SimpleTabbedPagedGui` with `PagedTab.of(button).addPageElements(...)`. Page operations apply to the **active** tab, switching tabs jumps to page 0. `open(player, tab, page)`.

---

## 6. Closing & exit flags (`GuiExitFlag`)

| Flag | Value | Effect |
|---|---|---|
| `DEFAULT` | 0 | Close normally – the parent GUI (if set) is reopened on the next tick |
| `CLOSE_PARENTS` | 1 | Do **not** reopen the parent (exit completely) |
| `SKIP_ONCLOSE` | 2 | Bit for `onClose` handling |
| `REDRAW` | 1 \| 2 | Used internally by `redraw()` |

```java
gui.close();                                  // DEFAULT → back to parent
gui.close(GuiExitFlag.CLOSE_PARENTS);         // close completely
gui.closeAll();                               // = close(CLOSE_PARENTS)
GuiExitFlag.getFlags(event.getExitFlag());    // evaluate in GuiCloseEvent
```

> **Pitfalls (v1.1):**
> - The `setOnClose` callback only runs in the listener when the `SKIP_ONCLOSE` bit **is set** (inverted logic) – i.e. on `redraw()`, but not on a normal close. For reliable close logic, catch `GuiCloseEvent` with a Bukkit listener (it is always fired).
> - `GuiExitFlag.isPresentIn(byte)` returns `true` when the flag is **not** set – use `getFlags(...).contains(...)` instead.
> - `setSlotCallbacks(Map)` is a no-op – call `setSlotCallback(...)` individually.

---

## 7. As a subclass (template methods)

Instead of a builder: extend `SimpleGui` / `SimplePagedGui` / `SimpleTabbedGui` / `SimpleTabbedPagedGui` and use the `protected` no-arg constructor. Configure via overridden hooks, fill in `init()`.

```java
public class ProfileGui extends SimpleGui {
    private final Player target;

    public ProfileGui(Player target) {
        super();              // builds the inventory from title()/size()/... and calls init()
        this.target = target; // careful: init() already ran – fields were still null there
    }

    @Override protected @NotNull Component title() { return MM.deserialize("<green>Profile"); }
    @Override protected int size() { return 27; }

    @Override protected void init() {
        setSlot(13, GuiElement.decoElement(Material.PLAYER_HEAD, "<yellow>Your profile"));
    }

    @Override protected boolean onClickGlobal(@NotNull GuiClickEvent event) { return true; }
}
```

More hooks: `type()`, `parent()`, `playerManipulation()`, `onOpen/onClose/onInteract/onDrag/onTick(...)`, for paged `pageSlots()`/`onPageChange`, for tabbed `tabSlots()`/`contentSlots()`/`onTabChange`.

---

## 8. Events (Bukkit listeners)

All events implement `IGuiEvent` → `getGui()` or `castGui(SimplePagedGui.class)`.

| Event | Base | Cancellable |
|---|---|---|
| `GuiClickEvent` | `InventoryClickEvent` (+ `getElement()`, `clickedOutside()`) | yes |
| `GuiOpenEvent` | `InventoryOpenEvent` | yes |
| `GuiCloseEvent` | `InventoryCloseEvent` (+ `getExitFlag()`) | – |
| `GuiDragEvent` / `GuiInteractEvent` | `InventoryDragEvent` / `InventoryInteractEvent` | yes |
| `GuiPageChangeEvent` / `GuiTabChangeEvent` | `GuiBaseEvent` | yes |
| `GuiTickEvent` | `GuiBaseEvent` | – |

Ticking (`startTicking()`/`stopTicking()`) starts automatically on `open` and stops on close – it runs on the Bukkit scheduler (not Folia-compatible, see [README](./README.md#4-important-threading--folia)).