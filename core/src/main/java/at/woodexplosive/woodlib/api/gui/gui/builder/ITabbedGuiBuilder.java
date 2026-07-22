package at.woodexplosive.woodlib.api.gui.gui.builder;

import at.woodexplosive.woodlib.api.gui.element.ITab;
import at.woodexplosive.woodlib.api.gui.event.GuiTabChangeEvent;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import at.woodexplosive.woodlib.api.gui.gui.ITabbedGui;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Fluent builder contract for {@link ITabbedGui}s, adding tab-slot / content-slot layout, tabs and a
 * tab-change callback on top of {@link IGuiBuilder}.
 *
 * @param <T> the concrete builder type (CRTP)
 * @param <G> the tabbed-GUI type produced by this builder
 */
public interface ITabbedGuiBuilder<T extends ITabbedGuiBuilder<T, G>, G extends ITabbedGui<G>> extends IGuiBuilder<T, G> {

    /**
     * Sets the slot indices the active tab's content is rendered into.
     * @param slots the content slots
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T setContentSlots(@NotNull List<Integer> slots);

    /**
     * Varargs variant of {@link #setContentSlots(List)}.
     * @param slots the content slots
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    default T setContentSlots(@NotNull Integer... slots) {
        return this.setContentSlots(List.of(slots));
    }

    /**
     * Appends a single slot to the content slots.
     * @param slot the content slot to add
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T addContentSlot(Integer slot);

    /**
     * Appends multiple slots to the content slots.
     * @param slots the content slots to add
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T addContentSlots(@NotNull Collection<Integer> slots);

    /**
     * Varargs variant of {@link #addContentSlots(Collection)}.
     * @param slots the content slots to add
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    default T addContentSlots(@NotNull Integer... slots) {
        return this.addContentSlots(List.of(slots));
    }

    /**
     * Removes a single slot from the content slots.
     * @param slot the content slot to remove
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T removeContentSlot(Integer slot);

    /**
     * Removes multiple slots from the content slots.
     * @param slots the content slots to remove
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T removeContentSlots(@NotNull Collection<Integer> slots);

    /**
     * Varargs variant of {@link #removeContentSlots(Collection)}.
     * @param slots the content slots to remove
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    default T removeContentSlots(@NotNull Integer... slots) {
        return this.removeContentSlots(List.of(slots));
    }

    /**
     * Sets the slot indices the tab buttons are rendered into.
     * @param slots the tab slots
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T setTabSlots(@NotNull List<Integer> slots);

    /**
     * Varargs variant of {@link #setTabSlots(List)}.
     * @param slots the tab slots
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    default T setTabSlots(@NotNull Integer... slots) {
        return this.setTabSlots(List.of(slots));
    }

    /**
     * Appends a single slot to the tab slots.
     * @param slot the tab slot to add
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T addTabSlot(Integer slot);

    /**
     * Appends multiple slots to the tab slots.
     * @param slots the tab slots to add
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T addTabSlots(@NotNull Collection<Integer> slots);

    /**
     * Varargs variant of {@link #addTabSlots(Collection)}.
     * @param slots the tab slots to add
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    default T addTabSlots(@NotNull Integer... slots) {
        return this.addTabSlots(List.of(slots));
    }

    /**
     * Removes a single slot from the tab slots.
     * @param slot the tab slot to remove
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T removeTabSlot(Integer slot);

    /**
     * Removes multiple slots from the tab slots.
     * @param slots the tab slots to remove
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T removeTabSlots(@NotNull Collection<Integer> slots);

    /**
     * Varargs variant of {@link #removeTabSlots(Collection)}.
     * @param slots the tab slots to remove
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    default T removeTabSlots(@NotNull Integer... slots) {
        return this.removeTabSlots(List.of(slots));
    }

    /**
     * Adds a tab to the GUI being built.
     * @param tab the tab to add
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T addTab(@NotNull ITab tab);

    /**
     * Sets the callback run when the active tab changes; returning {@code true} cancels the change.
     * @param onTabChange the {@link GuiTabChangeEvent} callback
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    T onTabChange(@NotNull IGui.Callback<GuiTabChangeEvent> onTabChange);
}
