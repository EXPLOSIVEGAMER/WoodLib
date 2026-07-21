package at.woodexplosive.woodlib.gui.element;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.item.AbstractItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;

/**
 * Default {@link IGuiElement} implementation: an {@link ItemStack} plus an optional
 * {@link ClickCallback}.
 *
 * <p>Build interactive elements with {@link #builder(Material)} (a {@link GuiElementBuilder}), or
 * create non-interactive decoration with the {@code decoElement(...)} factories. Use
 * {@link #empty()} for a blank slot.</p>
 */
public class GuiElement implements IGuiElement {

    protected final ClickCallback callback;
    protected final ItemStack item;

    /**
     * @param item the item to display
     * @param callback the click callback (use {@link #EMPTY_CALLBACK} for none)
     */
    public GuiElement(ItemStack item, ClickCallback callback) {
        this.item = item;
        this.callback = callback;
    }

    /**
     * Starts a builder for an interactive element of the given material and amount.
     * @param material the item material
     * @param amount the stack amount
     * @return a new {@link GuiElementBuilder}
     */
    public static GuiElementBuilder builder(Material material, int amount) {
        return new GuiElementBuilder(material, amount);
    }

    /**
     * Starts a builder for an interactive element of the given material (amount {@code 1}).
     * @param material the item material
     * @return a new {@link GuiElementBuilder}
     */
    public static GuiElementBuilder builder(Material material) {
        return new GuiElementBuilder(material);
    }

    /**
     * Creates a non-interactive decoration element.
     * @param material the item material
     * @param amount the stack amount
     * @param displayName the display name
     * @return a new decoration {@link GuiElement}
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static GuiElement decoElement(Material material, int amount, Component displayName) {
        return (GuiElement) new GuiElementBuilder(material, amount).displayName(displayName).buildElement();
    }

    /**
     * Decoration element with a {@link net.kyori.adventure.text.minimessage.MiniMessage MiniMessage}-formatted display name.
     * @param material the item material
     * @param amount the stack amount
     * @param displayName the MiniMessage display name
     * @return a new decoration {@link GuiElement}
     */
    public static GuiElement decoElement(Material material, int amount, String displayName) {
        return decoElement(material, amount, MM.deserialize(displayName));
    }

    /**
     * Decoration element (amount {@code 1}) with the given display name.
     * @param material the item material
     * @param displayName the display name
     * @return a new decoration {@link GuiElement}
     */
    public static GuiElement decoElement(Material material, Component displayName) {
        return decoElement(material, 1, displayName);
    }

    /**
     * Decoration element (amount {@code 1}) with a {@link net.kyori.adventure.text.minimessage.MiniMessage MiniMessage}-formatted display name.
     * @param material the item material
     * @param displayName the MiniMessage display name
     * @return a new decoration {@link GuiElement}
     */
    public static GuiElement decoElement(Material material, String displayName) {
        return decoElement(material, MM.deserialize(displayName));
    }

    /**
     * Plain decoration element (amount {@code 1}) with no custom name.
     * @param material the item material
     * @return a new decoration {@link GuiElement}
     */
    @Contract(value = "_ -> new", pure = true)
    public static GuiElement decoElement(Material material) {
        return (GuiElement) new GuiElementBuilder(material).buildElement();
    }

    /**
     * Decoration element built from an existing item builder's stack.
     * @param itemBuilder the item builder whose stack is used
     * @return a new decoration {@link GuiElement}
     */
    @Contract(value = "_ -> new", pure = true)
    public static GuiElement decoElement(AbstractItemBuilder<?> itemBuilder) {
        return (GuiElement) GuiElementBuilder.copyOf(itemBuilder).buildElement();
    }

    /**
     * Creates a new empty GuiElement
     * @return empty {@link GuiElement}
     */
    @Contract(value = "-> new", pure = true)
    public static GuiElement empty() {
        return new GuiElement(ItemStack.empty(), EMPTY_CALLBACK);
    }

    @Override
    public ClickCallback getCallback() {
        return this.callback;
    }

    @Override
    public GuiElement copy() {
        return (GuiElement) GuiElementBuilder.copyOf(this).buildElement();
    }

    @Override
    public ItemStack getItemStack() {
        return this.item;
    }
}
