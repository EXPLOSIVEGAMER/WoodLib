package at.woodexplosive.woodlib.item;

import at.woodexplosive.woodlib.api.item.AbstractItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Concrete {@link AbstractItemBuilder} for building plain {@link ItemStack}s.
 *
 * <p>Use the {@link #of(Material, int)} / {@link #copyOf(ItemStack)} factories or the constructors,
 * chain the fluent setters inherited from {@link AbstractItemBuilder}, then call
 * {@link #build()} to obtain a copy of the configured stack.</p>
 *
 * <pre>{@code
 * ItemStack sword = ItemBuilder.of(Material.DIAMOND_SWORD)
 *         .displayName("<red>Doom Blade")
 *         .setUnbreakable(true)
 *         .build();
 * }</pre>
 */
public class ItemBuilder extends AbstractItemBuilder<ItemBuilder> {

    private ItemBuilder() {}

    /**
     * Creates a builder for a fresh {@link ItemStack} of the given material and amount.
     *
     * @param material the item material
     * @param amount   the stack amount
     */
    public ItemBuilder(Material material, int amount) {
        this.item = ItemStack.of(material, amount);
    }

    /**
     * Creates a builder for a fresh {@link ItemStack} of the given material with amount {@code 1}.
     *
     * @param material the item material
     */
    public ItemBuilder(Material material) {
        this(material, 1);
    }

    /**
     * Creates a builder wrapping a copy of the given {@link ItemStack}; the original is untouched.
     *
     * @param item the stack to copy
     * @return a new builder holding a copy of {@code item}
     */
    public static ItemBuilder copyOf(ItemStack item) {
        return copyOf(item, ItemBuilder::new);
    }

    /**
     * Creates a builder for a fresh {@link ItemStack} of the given material and amount.
     *
     * @param material the item material
     * @param amount   the stack amount
     * @return a new builder
     */
    public static ItemBuilder of(Material material, int amount) {
        return of(material, amount, ItemBuilder::new);
    }

    /**
     * Creates a builder for a fresh {@link ItemStack} of the given material with amount {@code 1}.
     *
     * @param material the item material
     * @return a new builder
     */
    public static ItemBuilder of(Material material) {
        return of(material, ItemBuilder::new);
    }

}
