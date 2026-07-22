package at.woodexplosive.woodlib.gui.element;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.element.builder.IGuiElementBuilder;
import at.woodexplosive.woodlib.api.item.AbstractItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

/**
 * Builder for {@link GuiElement}s. Combines the full item-configuration API of
 * {@link AbstractItemBuilder} with a {@link IGuiElement.ClickCallback}, and produces the element via
 * {@link #buildElement()}.
 */
public class GuiElementBuilder extends AbstractItemBuilder<GuiElementBuilder> implements IGuiElementBuilder<GuiElementBuilder> {
    protected IGuiElement.ClickCallback callback = IGuiElement.EMPTY_CALLBACK;

    private GuiElementBuilder(ItemStack item) {
        this.item = item;
    }

    /**
     * @param material the item material
     * @param amount the stack amount
     */
    public GuiElementBuilder(Material material, int amount) {
        this.item = ItemStack.of(material, amount);
    }

    /**
     * @param material the item material (amount {@code 1})
     */
    public GuiElementBuilder(Material material) {
        this(material, 1);
    }

    /**
     * Creates a builder sharing the given element's item and callback (item not cloned).
     * @param element the element to wrap
     * @return a new builder
     */
    @Contract(value = "_ -> new", pure = true)
    public static GuiElementBuilder of(IGuiElement element) {
        return new GuiElementBuilder(element.getItemStack()).setCallback(element.getCallback());
    }

    /**
     * Creates a builder wrapping the given item stack directly (not cloned).
     * @param itemStack the item stack to wrap
     * @return a new builder
     */
    @Contract(value = "_ -> new", pure = true)
    public static GuiElementBuilder of(ItemStack itemStack) {
        return new GuiElementBuilder(itemStack);
    }

    /**
     * Creates a builder from a copy of the given element's item, keeping its callback.
     * @param element the element to copy
     * @return a new builder
     */
    @Contract(value = "_ -> new", pure = true)
    public static GuiElementBuilder copyOf(IGuiElement element) {
        return new GuiElementBuilder(element.getItemStack().clone()).setCallback(element.getCallback());
    }

    /**
     * Creates a builder from the stack produced by another item builder.
     * @param itemBuilder the item builder whose built stack is used
     * @return a new builder
     */
    @Contract(value = "_ -> new", pure = true)
    public static GuiElementBuilder copyOf(AbstractItemBuilder<?> itemBuilder) {
        return new GuiElementBuilder(itemBuilder.build());
    }

    /**
     * Creates a builder wrapping a copy of the given item stack.
     * @param itemStack the item stack to copy
     * @return a new builder
     */
    @Contract(value = "_ -> new", pure = true)
    public static GuiElementBuilder copyOf(ItemStack itemStack) {
        return new GuiElementBuilder(itemStack.clone());
    }

    @Contract(value = "_ -> this")
    @Override
    public GuiElementBuilder setCallback(IGuiElement.@NonNull ClickCallback callback) {
        this.callback = callback;
        return this;
    }

    @Contract(value = "-> new", pure = true)
    @Override
    public @NonNull GuiElement buildElement() {
        return new GuiElement(this.build(), this.callback);
    }
}