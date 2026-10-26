# WoodLib ItemBuilder (`ItemBuilder` / `AbstractItemBuilder`)

Fluent builder for `ItemStack`s based on modern Paper components (item model, CustomModelData component, rarity, max damage, tooltip control). `AbstractItemBuilder` is also the base of `GuiElementBuilder` ([gui.md](./gui.md)) – every method here exists there as well.

**Package:** `at.woodexplosive.woodlib.impl.item.ItemBuilder`, `at.woodexplosive.woodlib.api.item.AbstractItemBuilder`

---

## 1. Quick start

```java
ItemStack sword = ItemBuilder.of(Material.DIAMOND_SWORD)
        .displayName("<red><bold>Dragon Fang")              // MiniMessage
        .stringLore("<gray>A legendary blade.", "", "<gold>Legendary")
        .addEnchantment(Enchantment.SHARPNESS, 5)
        .setUnbreakable(true)
        .rarity(ItemRarity.EPIC)
        .build();

ItemStack copy = ItemBuilder.copyOf(sword).amount(3).build(); // original stays unchanged
```

Entry points: `ItemBuilder.of(Material[, amount])`, `new ItemBuilder(Material[, amount])` or `ItemBuilder.copyOf(ItemStack)` (clones). `build()` returns a **copy** of the configured stack.

---

## 2. Method overview

### 2.1 Basics

| Method | Description |
|---|---|
| `amount(int)` | Clamped to 1–99; raises `maxStackSize` if needed |
| `material(Material)` | Swap material, meta is kept if compatible |
| `rarity(ItemRarity)` | Name color (`COMMON`, `UNCOMMON`, `RARE`, `EPIC`) |
| `damage(int)` / `maxDamage(int)` | Durability (only items with durability) |
| `setUnbreakable(bool)` | Unbreakable |
| `editMeta(Consumer<ItemMeta>)` / `editMeta(MetaClass, Consumer)` | Direct meta access (e.g. `PotionMeta`) |
| `build()` | Return the finished `ItemStack` (copy) |

### 2.2 Name, lore, tooltip

| Method | Description |
|---|---|
| `displayName(Component / String)` | Display name; `String` = MiniMessage |
| `lore(List<Component> / Component...)` | Set lore (replaces) |
| `stringLore(List<String> / String...)` | Lore as MiniMessage |
| `hideVanillaTooltip()` | Hide all vanilla tooltips |
| `removeToolTip(bool)` | Hide the entire tooltip |
| `addItemFlags(...)` / `removeItemFlags(...)` | `ItemFlag`s |
| `hideEnchantments()` / `hideAttributes()` | Hide enchantment or attribute lines |

### 2.3 Enchantments

| Method | Description |
|---|---|
| `addEnchantment(Enchantment, level)` | Unsafe – any level allowed |
| `setEnchantable(int)` | Enchantability value (enchanting table) |
| `setEnchantmentGlintOverride(Boolean)` | Force glint (`true`), forbid (`false`), default (`null`) |

### 2.4 Resource pack / models

| Method | Description |
|---|---|
| `setItemModel(NamespacedKey)` | Item model component (e.g. `myplugin:shop_coin`) |
| `setCustomModelData(float)` | CustomModelData with a single float value |
| `setCustomModelData(CustomModelDataComponent)` | Full CMD component (floats, flags, strings, colors) |

### 2.5 PersistentData

| Method | Description |
|---|---|
| `setPDC(key, type, value)` | Write a value into the `PersistentDataContainer` |
| `removePDC(key)` | Remove a key |
| `getPDC()` | **Read-only snapshot** (copy) – writing to it has no effect, use `setPDC` instead |

### 2.6 Heads (`PLAYER_HEAD`)

| Method | Description |
|---|---|
| `skullTexture(String base64)` | Texture via Base64 value |
| `skullURL(String url)` | Texture via skin URL (invalid URL → error in log, texture unchanged) |
| `skullOwner(OfflinePlayer)` | A player's head |

### 2.7 Attributes

| Method | Description |
|---|---|
| `addAttribute(attr, key, value, operation[, EquipmentSlot])` | Build + add a modifier (without slot = any slot) |
| `addAttribute(attr, modifier)` / `addAttributes(attr, modifiers...)` | Add ready-made modifiers |
| `setAttributes(Multimap)` | Replace all – also overrides vanilla defaults; `null` = restore defaults |
| `removeAttribute(attr[, modifier])` / `removeAttributes(attr, modifiers...)` | Remove specific or all modifiers of an attribute |
| `removeAttribute(EquipmentSlot)` | Remove all modifiers for a slot |

```java
ItemStack boots = ItemBuilder.of(Material.LEATHER_BOOTS)
        .addAttribute(Attribute.MOVEMENT_SPEED, new NamespacedKey(plugin, "speed_boots"),
                0.1, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.FEET)
        .hideAttributes()
        .build();
```

### 2.8 Custom blocks

| Method | Description |
|---|---|
| `linkCustomBlock(ICustomBlock)` | Stamps the block ID into the PDC → right-click places the custom block, breaking it drops the item again |

The item must additionally be registered as a drop: `CustomBlockRegistry.register(block, item)` – see [custom-blocks.md](./custom-blocks.md).

---

## 3. Deriving your own builders

`AbstractItemBuilder<SELF>` is generic via CRTP – so your own builders keep the chaining:

```java
public class ShopItemBuilder extends AbstractItemBuilder<ShopItemBuilder> {
    public ShopItemBuilder() {} // for the factory helpers

    public static ShopItemBuilder of(Material material) {
        return AbstractItemBuilder.of(material, ShopItemBuilder::new);
    }

    public ShopItemBuilder price(int coins) {
        return stringLore("<gray>Price: <gold>" + coins + " Coins");
    }
}
```

Factory helpers: `AbstractItemBuilder.of(material[, amount], factory)` and `copyOf(stack, factory)` – `factory` supplies an empty builder, the helper sets the `item`.