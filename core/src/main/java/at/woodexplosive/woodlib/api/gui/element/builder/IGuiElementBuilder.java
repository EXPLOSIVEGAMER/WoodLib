package at.woodexplosive.woodlib.api.gui.element.builder;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Fluent builder contract for {@link IGuiElement}s: configure the click behavior and produce the
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
    @Contract(value = "_ -> this")
    T setCallback(@NotNull IGuiElement.ClickCallback callback);

    /**
     * Builds the Gui Element
     * @return The built GuiElement
     */
    @Contract(value = "-> new", pure = true)
    @NotNull IGuiElement buildElement();
}
