package at.woodexplosive.woodlib.api.gui.gui.builder;

import at.woodexplosive.woodlib.api.gui.event.GuiPageChangeEvent;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import at.woodexplosive.woodlib.api.gui.gui.IPagedGui;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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
     * Appends a single slot to the page slots.
     * @param slot the page slot to add
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T addPageSlot(Integer slot);

    /**
     * Appends multiple slots to the page slots.
     * @param slots the page slots to add
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T addPageSlots(@NotNull Collection<Integer> slots);

    /**
     * Varargs variant of {@link #addPageSlots(Collection)}.
     * @param slots the page slots to add
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    default T addPageSlots(@NotNull Integer... slots) {
        return this.addPageSlots(List.of(slots));
    }

    /**
     * Removes a single slot from the page slots.
     * @param slot the page slot to remove
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T removePageSlot(Integer slot);

    /**
     * Removes multiple slots from the page slots.
     * @param slots the page slots to remove
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T removePageSlots(@NotNull Collection<Integer> slots);

    /**
     * Varargs variant of {@link #removePageSlots(Collection)}.
     * @param slots the page slots to remove
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    default T removePageSlots(@NotNull Integer... slots) {
        return this.removePageSlots(List.of(slots));
    }

    /**
     * Sets the callback run when the page changes; returning {@code true} cancels the change.
     * @param onPageChange the {@link GuiPageChangeEvent} callback
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T setOnPageChange(@NotNull IGui.Callback<GuiPageChangeEvent> onPageChange);
}
