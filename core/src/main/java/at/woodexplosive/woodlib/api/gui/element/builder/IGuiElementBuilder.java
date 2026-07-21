package at.woodexplosive.woodlib.api.gui.element.builder;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Contract;

/**
 * Fluent builder contract for {@link IGuiElement}s: configure the click behaviour and produce the
 * element via {@link #buildElement()}.
 *
 * @param <T> the concrete builder type, for fluent self-returning methods (CRTP)
 */
public interface IGuiElementBuilder<T extends IGuiElementBuilder<T>> {

    /** Shared {@link MiniMessage} instance for deserializing user-facing strings. */
    MiniMessage MM = MiniMessage.miniMessage();

    /**
     * Set the callback to execute when this element
     * is clicked inside a gui.
     *
     * @param callback the {@link IGuiElement.ClickCallback}
     * @return this element builder
     */
    T setCallback(IGuiElement.ClickCallback callback);

    /**
     * Builds the Gui Element
     * @return The built GuiElement
     */
    @Contract(value = "-> new", pure = true)
    IGuiElement buildElement();
}
