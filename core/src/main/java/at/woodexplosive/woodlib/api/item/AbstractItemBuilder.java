package at.woodexplosive.woodlib.api.item;

import at.woodexplosive.woodlib.WoodLib;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractItemBuilder<SELF extends AbstractItemBuilder<SELF>> {

    protected static final MiniMessage MM = MiniMessage.miniMessage();

    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    protected final SELF self() {
        return (SELF) this;
    }

    /**
     * Creates a new builder wrapping a copy of the given ItemStack.
     * <br>The stack is cloned, so the original is left untouched.
     *
     * @param item    the ItemStack to copy
     * @param factory supplies the concrete builder instance (e.g. {@code ItemBuilder::new})
     * @param <T>     the concrete builder type
     * @return a new builder holding a copy of the given ItemStack
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <T extends AbstractItemBuilder<T>> T copyOf(ItemStack item, Supplier<T> factory) {
        T builder = factory.get();
        builder.item = item.clone();
        return builder;
    }

    /**
     * Creates a new builder for a fresh ItemStack of the given material and amount.
     *
     * @param material the {@link Material} of the new ItemStack
     * @param amount   the stack amount
     * @param factory  supplies the concrete builder instance (e.g. {@code ItemBuilder::new})
     * @param <T>      the concrete builder type
     * @return a new builder holding the created ItemStack
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <T extends AbstractItemBuilder<T>> T of(Material material, int amount, Supplier<T> factory) {
        T builder = factory.get();
        builder.item = ItemStack.of(material, amount);
        return builder;
    }

    /**
     * Creates a new builder for a fresh ItemStack of the given material with amount {@code 1}.
     *
     * @param material the {@link Material} of the new ItemStack
     * @param factory  supplies the concrete builder instance (e.g. {@code ItemBuilder::new})
     * @param <T>      the concrete builder type
     * @return a new builder holding the created ItemStack
     * @see #of(Material, int, Supplier)
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <T extends AbstractItemBuilder<T>> T of(Material material, Supplier<T> factory) {
        return of(material, 1, factory);
    }

    protected ItemStack item;

    /**
     * Sets the Item amount of this ItemStack
     * <br>{@code clamped 1 - 99}
     * <br>If amount > getMaxStackSize() then setMaxStackSize to amount
     *
     * @param amount amount
     * @return this builder for chaining
     */
    public SELF amount(int amount) {
        final int a = Math.clamp(amount, 1, 99);
        if (a > this.item.getMaxStackSize()) this.item.editMeta(m -> m.setMaxStackSize(a));
        this.item.setAmount(a);
        return self();
    }

    /**
     * Sets the Display Name of this ItemStack
     *
     * @param displayName The Display Name
     * @return this builder for chaining
     */
    public SELF displayName(Component displayName) {
        this.item.editMeta(m -> m.displayName(displayName));
        return self();
    }

    /**
     * @see #displayName(Component)
     */
    public SELF displayName(String displayName) {
        return this.displayName(MM.deserialize(displayName));
    }

    /**
     * Sets the Lore of this ItemStack
     *
     * @param lore The Lore
     * @return this builder for chaining
     */
    public SELF lore(List<Component> lore) {
        this.item.editMeta(m -> m.lore(lore));
        return self();
    }

    /**
     * @see #lore(List)
     */
    public SELF lore(@NotNull Component... lore) {
        return this.lore(List.of(lore));
    }

    /**
     * Sets the lore using {@link net.kyori.adventure.text.minimessage.MiniMessage} of this ItemStack
     *
     * @param lore The lore
     * @return this builder for chaining
     * @see #lore(List)
     */
    public SELF stringLore(List<String> lore) {
        return this.lore(lore.stream().map(MM::deserialize).toList());
    }

    /**
     * @see #stringLore(List)
     */
    public SELF stringLore(@NotNull String... lore) {
        return this.stringLore(List.of(lore));
    }

    /**
     * Hides all vanilla tooltips of this ItemStack
     *
     * @return this builder for chaining
     */
    public SELF hideVanillaTooltip() {
        this.item.editMeta(m -> m.addItemFlags(ItemFlag.values()));
        return self();
    }

    /**
     * Sets whether the entire tooltip of this ItemStack is hidden.
     *
     * @param hide {@code true} to hide the whole tooltip, {@code false} to show it
     * @return this builder for chaining
     */
    public SELF removeToolTip(boolean hide) {
        this.item.editMeta(m -> m.setHideTooltip(hide));
        return self();
    }

    /**
     * Adds ItemFlags to this ItemStack
     *
     * @param itemFlags ItemFlags
     * @return this builder for chaining
     */
    public SELF addItemFlags(ItemFlag... itemFlags) {
        this.item.editMeta(m -> m.addItemFlags(itemFlags));
        return self();
    }

    /**
     * Removes Item Flags of this ItemStack
     *
     * @param itemFlags ItemFlags
     * @return this builder for chaining
     */
    public SELF removeItemFlags(ItemFlag... itemFlags) {
        this.item.removeItemFlags(itemFlags);
        return self();
    }

    /**
     * Hides the Enchantment Tooltips of this ItemStack
     *
     * @return this builder for chaining
     */
    public SELF hideEnchantments() {
        item.editMeta(m -> m.addItemFlags(ItemFlag.HIDE_ENCHANTS));
        return self();
    }

    /**
     * Sets the Enchantable value of this ItemStack
     *
     * @param value the enchantability value used when enchanting this item
     * @return this builder for chaining
     * @see org.bukkit.inventory.meta.ItemMeta#setEnchantable(Integer)
     */
    public SELF setEnchantable(int value) {
        item.editMeta(m -> m.setEnchantable(value));
        return self();
    }

    /**
     * Sets the enchantment_glint_override of this {@link ItemStack}
     * @param override {@code true}/{@code false} to force, or {@code null} to reset to vanilla default
     * @return this builder for chaining
     * @see ItemMeta#setEnchantmentGlintOverride(Boolean)
     */
    public SELF setEnchantmentGlintOverride(@Nullable Boolean override) {
        item.editMeta(m -> m.setEnchantmentGlintOverride(override));
        return self();
    }

    /**
     * Adds an Enchantment to this ItemStack
     *
     * @param enchantment {@link Enchantment}
     * @param level       Level
     * @return this builder for chaining
     */
    public SELF addEnchantment(Enchantment enchantment, int level) {
        item.editMeta(m -> m.addEnchant(enchantment, level, true));
        return self();
    }

    /**
     * Sets the Item Model of this ItemStack
     *
     * @param itemModel Item Model
     * @return this builder for chaining
     */
    public SELF setItemModel(NamespacedKey itemModel) {
        item.editMeta(m -> m.setItemModel(itemModel));
        return self();
    }

    /**
     * Sets the CustomModelData of this ItemStack
     *
     * @param data {@link CustomModelDataComponent}
     * @return this builder for chaining
     */
    @SuppressWarnings("UnstableApiUsage")
    public SELF setCustomModelData(CustomModelDataComponent data) {
        item.editMeta(m -> m.setCustomModelDataComponent(data));
        return self();
    }

    /**
     * Sets the CustomModelData of this ItemStack to a single float value
     *
     * @param value the float value written to the custom model data component
     * @return this builder for chaining
     */
    @SuppressWarnings("UnstableApiUsage")
    public SELF setCustomModelData(float value) {
        item.editMeta(m -> {
            CustomModelDataComponent cmd = m.getCustomModelDataComponent();
            cmd.setFloats(List.of(value));
            m.setCustomModelDataComponent(cmd);
        });
        return self();
    }

    /**
     * Sets the unbreakable tag of this ItemStack
     *
     * @param unbreakable {@code true} to make the item unbreakable, {@code false} otherwise
     * @return this builder for chaining
     */
    public SELF setUnbreakable(boolean unbreakable) {
        this.item.editMeta(m -> m.setUnbreakable(unbreakable));
        return self();
    }

    /**
     * Sets the rarity of this ItemStack
     *
     * @param rarity {@link ItemRarity}
     * @return this builder for chaining
     */
    public SELF rarity(ItemRarity rarity) {
        this.item.editMeta(m -> m.setRarity(rarity));
        return self();
    }

    /**
     * Sets the Damage (Durability) of this ItemStack
     * <br>Only works on items that have durability
     *
     * @param damage amount of damage
     * @return this builder for chaining
     */
    public SELF damage(int damage) {
        this.item.editMeta(Damageable.class, m -> m.setDamage(damage));
        return self();
    }

    /**
     * Sets the Max Damage (Durability) of this ItemStack
     * <br>Only works on items that have durability
     *
     * @param max Max Damage
     * @return this builder for chaining
     */
    public SELF maxDamage(int max) {
        this.item.editMeta(Damageable.class, m -> m.setMaxDamage(max));
        return self();
    }

    /**
     * Gets a read-only snapshot of the {@link PersistentDataContainer} of this ItemStack.
     * <br><b>Note:</b> this returns the container of a <i>copy</i> of the ItemMeta, so writing to it
     * does <b>not</b> persist to the item. Use {@link #setPDC(NamespacedKey, PersistentDataType, Object)}
     * to store values.
     *
     * @return a read-only {@link PersistentDataContainer} snapshot
     */
    @Contract(pure = true)
    public PersistentDataContainer getPDC() {
        return this.item.getItemMeta().getPersistentDataContainer();
    }

    /**
     * Sets a value in the {@link PersistentDataContainer} of this ItemStack
     *
     * @param key   the {@link NamespacedKey}
     * @param type  the {@link PersistentDataType}
     * @param value the value (value has to match the complex type {@code C})
     * @param <P>   primitive Type
     * @param <C>   complex Type
     * @return this builder for chaining
     */
    public <P, C> SELF setPDC(NamespacedKey key, PersistentDataType<P, C> type, C value) {
        this.item.editMeta(m -> {
            PersistentDataContainer pdc = m.getPersistentDataContainer();
            pdc.set(key, type, value);
        });
        return self();
    }

    /**
     * Removes the value stored under the given key from the {@link PersistentDataContainer} of this ItemStack.
     * <br>Does nothing if no value is stored under that key.
     *
     * @param key the {@link NamespacedKey} to remove
     * @return this builder for chaining
     */
    public SELF removePDC(NamespacedKey key) {
        this.item.editMeta(m -> {
            PersistentDataContainer pdc = m.getPersistentDataContainer();
            pdc.remove(key);
        });
        return self();
    }

    /**
     * Sets a custom texture on a player head using a Base64 texture value.
     * <br>Only works on {@link Material#PLAYER_HEAD}
     * @param base64 the Base64-encoded texture value taken from the "textures" property
     * @return this builder for chaining
     */
    public SELF skullTexture(String base64) {
        this.item.editMeta(SkullMeta.class, m -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
            profile.setProperty(new ProfileProperty("textures", base64));
            m.setPlayerProfile(profile);
        });
        return self();
    }

    /**
     * Sets a custom texture on a player head using a skin URL.
     * <br>Only works on {@link Material#PLAYER_HEAD}
     * <br>If the URL is malformed the texture is left unchanged and an error is logged.
     * @param url the URL of the skin texture
     * @return this builder for chaining
     */
    public SELF skullURL(String url) {
        this.item.editMeta(SkullMeta.class, m -> {
            try {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
                profile.getTextures().setSkin(URI.create(url).toURL());
                m.setPlayerProfile(profile);
            } catch (MalformedURLException e) {
                WoodLib.logger().error("Skull URL is Malformed", e);
            }
        });
        return self();
    }

    /**
     * Sets the owner of a player head.
     * <br>Only works on {@link Material#PLAYER_HEAD}
     * @param player the {@link OfflinePlayer} whose skin is used
     * @return this builder for chaining
     */
    public SELF skullOwner(OfflinePlayer player) {
        this.item.editMeta(SkullMeta.class, m -> m.setOwningPlayer(player));
        return self();
    }

    /**
     * Replaces all attribute modifiers of this ItemStack with the given ones.
     * <br>Passing {@code null} clears the modifiers and restores the vanilla defaults for the material.
     * <br><b>Note:</b> a non-null map also overrides the material's vanilla default modifiers.
     * Use {@link #addAttribute(Attribute, AttributeModifier)} to keep them and add on top.
     *
     * @param attributes the attributes with their modifiers to set, or {@code null} to reset to defaults, or an empty {@link Multimap} to remove all Attributes
     * @return this builder for chaining
     * @see ItemMeta#setAttributeModifiers(Multimap)
     */
    public SELF setAttributes(@Nullable Multimap<Attribute, AttributeModifier> attributes) {
        this.item.editMeta(m -> m.setAttributeModifiers(attributes));
        return self();
    }

    /**
     * Adds multiple modifiers for the same attribute to this ItemStack.
     *
     * @param attribute the {@link Attribute} the modifiers apply to
     * @param modifiers the {@link AttributeModifier modifiers} to add
     * @return this builder for chaining
     */
    public SELF addAttributes(Attribute attribute, AttributeModifier... modifiers) {
        for (AttributeModifier modifier : modifiers) {
            this.item.editMeta(m -> m.addAttributeModifier(attribute, modifier));
        }
        return self();
    }

    /**
     * Adds a single modifier for the given attribute to this ItemStack.
     *
     * @param attribute the {@link Attribute} the modifier applies to
     * @param modifier  the {@link AttributeModifier} to add
     * @return this builder for chaining
     */
    public SELF addAttribute(Attribute attribute, AttributeModifier modifier) {
        this.item.editMeta(m -> m.addAttributeModifier(attribute, modifier));
        return self();
    }

    /**
     * Builds and adds an {@link AttributeModifier} for the given attribute, restricted to a slot group.
     *
     * @param attribute the {@link Attribute} the modifier applies to
     * @param key       the unique {@link NamespacedKey} identifying the modifier
     * @param value     the modifier amount
     * @param operation how the value is applied ({@link AttributeModifier.Operation})
     * @param slot the {@link EquipmentSlot} the modifier is active in
     * @return this builder for chaining
     */
    @SuppressWarnings("UnstableApiUsage")
    public SELF addAttribute(Attribute attribute, NamespacedKey key, double value, AttributeModifier.Operation operation, EquipmentSlot slot) {
        this.item.editMeta(m -> m.addAttributeModifier(attribute, new AttributeModifier(key, value, operation, slot.getGroup())));
        return self();
    }

    /**
     * Builds and adds an {@link AttributeModifier} for the given attribute, active in any slot.
     *
     * @param attribute the {@link Attribute} the modifier applies to
     * @param key       the unique {@link NamespacedKey} identifying the modifier
     * @param value     the modifier amount
     * @param operation how the value is applied ({@link AttributeModifier.Operation})
     * @return this builder for chaining
     */
    public SELF addAttribute(Attribute attribute, NamespacedKey key, double value, AttributeModifier.Operation operation) {
        this.item.editMeta(m -> m.addAttributeModifier(attribute, new AttributeModifier(key, value, operation)));
        return self();
    }

    /**
     * Removes multiple specific modifiers of the given attribute from this ItemStack.
     *
     * @param attribute the {@link Attribute} the modifiers belong to
     * @param modifiers the {@link AttributeModifier modifiers} to remove
     * @return this builder for chaining
     */
    public SELF removeAttributes(Attribute attribute, AttributeModifier... modifiers) {
        for (AttributeModifier modifier : modifiers) {
            this.item.editMeta(m -> m.removeAttributeModifier(attribute, modifier));
        }
        return self();
    }

    /**
     * Removes a single specific modifier of the given attribute from this ItemStack.
     *
     * @param attribute the {@link Attribute} the modifier belongs to
     * @param modifier  the {@link AttributeModifier} to remove
     * @return this builder for chaining
     */
    public SELF removeAttribute(Attribute attribute, AttributeModifier modifier) {
        this.item.editMeta(m -> m.removeAttributeModifier(attribute, modifier));
        return self();
    }

    /**
     * Removes all modifiers of the given attribute from this ItemStack.
     *
     * @param attribute the {@link Attribute} whose modifiers are removed
     * @return this builder for chaining
     */
    public SELF removeAttribute(Attribute attribute) {
        this.item.editMeta(m -> m.removeAttributeModifier(attribute));
        return self();
    }

    /**
     * Removes all attribute modifiers of this ItemStack that apply to the given equipment slot.
     *
     * @param slot the {@link EquipmentSlot} whose modifiers are removed
     * @return this builder for chaining
     */
    public SELF removeAttribute(EquipmentSlot slot) {
        this.item.editMeta(m -> m.removeAttributeModifier(slot));
        return self();
    }

    /**
     * Hides the Attribute Tooltips of this {@link ItemStack}
     * @return this builder for chaining
     */
    public SELF hideAttributes() {
        this.item.editMeta(m -> m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES));
        return self();
    }

    /**
     * Edits the {@link ItemMeta} of this stack if the meta is of the specified type.
     * @param metaClass the type of meta to edit
     * @param consumer the meta consumer
     * @param <M> the meta type
     * @return this builder for chaining
     * @see ItemStack#editMeta(Class, Consumer)
     */
    public <M extends ItemMeta> SELF editMeta(Class<M> metaClass, Consumer<? super M> consumer) {
        this.item.editMeta(metaClass, consumer);
        return self();
    }

    /**
     * Edits the {@link ItemMeta} of this stack.
     * @param consumer the meta consumer
     * @return this builder for chaining
     * @see ItemStack#editMeta(Consumer)
     */
    public SELF editMeta(Consumer<? super ItemMeta> consumer) {
        this.item.editMeta(consumer);
        return self();
    }

    /**
     * Sets the Material of this {@link ItemStack} to the new Material
     * <br>Keeps ItemMeta if compatible
     * @param material The Material
     * @return this builder for chaining
     */
    public SELF material(Material material) {
        this.item = this.item.withType(material);
        return self();
    }

    /**
     * Builds this configured {@link ItemStack}
     * @return a copy of the built ItemStack
     */
    @Contract(value = "-> new", pure = true)
    public ItemStack build() {
        return this.item.clone();
    }
}
