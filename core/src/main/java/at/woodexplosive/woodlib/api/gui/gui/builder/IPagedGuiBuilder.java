package at.woodexplosive.woodlib.api.gui.gui.builder;

import at.woodexplosive.woodlib.api.gui.event.GuiPageChangeEvent;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import at.woodexplosive.woodlib.api.gui.gui.IPagedGui;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Fluent builder contract for {@link IPagedGui}s, adding page-slot layout and a page-change callback
 * on top of {@link IGuiBuilder}.
 *
 * @param <T> the concrete builder type (CRTP)
 * @param <G> the paged-GUI type produced by this builder
 */
public interface IPagedGuiBuilder<T extends IPagedGuiBuilder<T, G>, G extends IPagedGui<G>> extends IGuiBuilder<T, G> {

    /**
     * Sets the slot indices that page elements are laid out into. Defaults to every slot.
     * @param pageSlots the page slots
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T setPageSlots(@NotNull List<Integer> pageSlots);

    /**
     * Varargs variant of {@link #setPageSlots(List)}.
     * @param pageSlots the page slots
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    default T setPageSlots(@NotNull Integer... pageSlots) {
        return this.setPageSlots(List.of(pageSlots));
    }

    /**
     * Sets the callback run when the page changes; returning {@code true} cancels the change.
     * @param onPageChange the {@link GuiPageChangeEvent} callback
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T onPageChange(@NotNull IGui.Callback<GuiPageChangeEvent<G>> onPageChange);
}
