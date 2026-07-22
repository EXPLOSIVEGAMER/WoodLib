package at.woodexplosive.woodlib.api.gui.gui;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.gui.element.GuiElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * An {@link IGui} that spreads a list of "page elements" across a fixed set of slots over multiple
 * pages, with paging navigation.
 *
 * <p>Elements are added via {@link #addPageElement(IGuiElement)} and laid out into the configured
 * {@link #getPageSlots() page slots} by {@link #populatePage()}. Navigation is done with
 * {@link #nextPage()} / {@link #previousPage()} / {@link #setPage(int)}; most layout math is provided
 * as default methods.</p>
 *
 * @param <T> the concrete paged-GUI type, for fluent self-returning methods (CRTP)
 */
public interface IPagedGui<T extends IPagedGui<T>> extends IGui<T> {

    /**
     * Returns the backing list of all page elements (across every page).
     * @return the page elements
     */
    @Contract(value = "-> _", pure = true)
    @NotNull LinkedList<IGuiElement> getPageElements();

    /**
     * Appends a single element to the page elements.
     * @param element the element to add
     * @return this GUI for chaining
     */
    @Contract(value = "_ -> this")
    T addPageElement(@NotNull IGuiElement element);

    /**
     * Appends multiple elements to the page elements.
     * @param elements the elements to add
     * @return this GUI for chaining
     */
    @Contract(value = "_ -> this")
    T addPageElements(@NotNull Collection<? extends IGuiElement> elements);

    /**
     * Varargs variant of {@link #addPageElements(Collection)}.
     * @param elements the elements to add
     * @return this GUI for chaining
     */
    @Contract(value = "_ -> this")
    default T addPageElements(@NotNull IGuiElement... elements) {
        return this.addPageElements(List.of(elements));
    }

    /**
     * Removes a single element from the page elements.
     * @param element the element to remove
     * @return this GUI for chaining
     */
    @Contract(value = "_ -> this")
    T removePageElement(@NotNull IGuiElement element);

    /**
     * Removes multiple elements from the page elements.
     * @param elements the elements to remove
     * @return this GUI for chaining
     */
    @Contract(value = "_ -> this")
    T removePageElements(@NotNull Collection<? extends IGuiElement> elements);

    /**
     * Varargs variant of {@link #removePageElements(Collection)}.
     * @param elements the elements to remove
     * @return this GUI for chaining
     */
    @Contract(value = "_ -> this")
    default T removePageElements(@NotNull IGuiElement... elements) {
        return this.removePageElements(List.of(elements));
    }

    /**
     * Replaces all page elements with the given list.
     * @param elements the new page elements
     * @return this GUI for chaining
     */
    @Contract(value = "_ -> this")
    T setPageElement(@NotNull LinkedList<? extends IGuiElement> elements);

    /**
     * Returns the current page index (0-based).
     * @return the current page
     */
    @Contract(value = "-> _", pure = true)
    int getPage();

    /**
     * Switches to the given page (fires a {@link at.woodexplosive.woodlib.api.gui.event.GuiPageChangeEvent}).
     * @param page the target page index
     * @return this GUI for chaining
     */
    @Contract(value = "_ -> this")
    T setPage(int page);

    /**
     * Returns the slot indices that page elements are laid out into.
     * @return the page slots
     */
    @Contract(value = "-> _", pure = true)
    @NotNull List<Integer> getPageSlots();

    /**
     * Places an element at {@code slot} that advances to the next page when clicked. If the element
     * has no callback of its own, a "next page" callback is attached automatically.
     * @param slot the slot to place the control in
     * @param element the element to use as the control
     * @return this GUI for chaining
     */
    @Contract(value = "_, _ -> this")
    T setNextPageElement(int slot, @NotNull IGuiElement element);

    /**
     * Places an element at {@code slot} that goes back to the previous page when clicked. If the
     * element has no callback of its own, a "previous page" callback is attached automatically.
     * @param slot the slot to place the control in
     * @param element the element to use as the control
     * @return this GUI for chaining
     */
    @Contract(value = "_, _ -> this")
    T setPreviousPageElement(int slot, @NotNull IGuiElement element);

    /** Clears the page slots and fills them with the elements of the current page. */
    default void populatePage() {
        this.clearPageSlots();

        int first = this.getFirstElementIndex();
        int last = this.getLastElementIndex();

        for (int i = first; i < last; i++) {
            int slot = this.getPageSlots().get(i - first);
            this.setSlot(slot, this.getPageElements().get(i));
        }
    }

    /** Empties all page slots (fills them with {@link GuiElement#empty()}). */
    default void clearPageSlots() {
        this.getPageSlots().forEach(slot -> this.setSlot(slot, GuiElement.empty()));
    }

    /**
     * Index into {@link #getPageElements()} of the first element shown on the current page.
     * @return the first element index of the current page
     */
    @Contract(value = "-> _", pure = true)
    default int getFirstElementIndex() {
        return this.getPage() * this.getElementsPerPage();
    }

    /**
     * Exclusive index into {@link #getPageElements()} just past the last element of the current page.
     * @return the end index of the current page
     */
    @Contract(value = "-> _", pure = true)
    default int getLastElementIndex() {
        return Math.min(this.getFirstElementIndex() + this.getElementsPerPage(), getPageElements().size());
    }

    /**
     * Number of elements that fit on one page, i.e. the number of {@link #getPageSlots() page slots}.
     * @return the page capacity
     */
    @Contract(value = "-> _", pure = true)
    default int getElementsPerPage() {
        return this.getPageSlots().size();
    }

    /**
     * Highest valid (0-based) page index for the current element count.
     * @return the last page index, never negative
     */
    @Contract(value = "-> _", pure = true)
    default int getMaxPage() {
        return Math.max(0, Math.ceilDiv(this.getPageElements().size(), this.getElementsPerPage()) - 1);
    }

    /** Advances to the next page, clamped to {@link #getMaxPage()}. */
    default void nextPage() {
        int newPage = Math.min(this.getMaxPage(), this.getPage() + 1);
        this.setPage(newPage);
    }

    /** Goes back to the previous page, clamped to page {@code 0}. */
    default void previousPage() {
        int newPage = Math.max(0, this.getPage() - 1);
        this.setPage(newPage);
    }
}
