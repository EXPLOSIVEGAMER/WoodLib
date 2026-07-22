package at.woodexplosive.woodlib.api.gui.element;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.gui.gui.ITabbedGui;
import at.woodexplosive.woodlib.gui.element.GuiElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Data model for a single tab of a tabbed GUI. A tab is <b>not</b> a GUI of its own; it holds the
 * button element used to select it plus its content (a {@code slot → element} map). The owning tabbed
 * GUI renders the active tab into its inventory via {@link #populateContents(ITabbedGui)}.
 *
 * <p>Content is keyed by slot. {@link #setContentElement(int, IGuiElement)} places an element at a
 * specific slot, while {@link #addContentElement(IGuiElement)} fills the next free content slot (see
 * {@link #nextFreeContentSlot()}).</p>
 */
public interface ITab {

    /**
     * Returns the button element shown in the tabbed GUI to select this tab.
     * @return the tab button element
     */
    @Contract(pure = true)
    IGuiElement getTabElement();

    /**
     * Sets the button element shown in the tabbed GUI to select this tab.
     * @param element the tab button element
     * @return this tab for chaining
     */
    ITab setTabElement(@NotNull IGuiElement element);

    /**
     * Returns the slot indices this tab may fill with content, in order.
     * @return the content slots
     */
    @Contract(pure = true)
    List<Integer> getContentSlots();

    /**
     * Sets the slot indices this tab may fill with content. The owning tabbed GUI assigns its own
     * content slots to each tab when the tab is added.
     * @param slots the content slots
     * @return this tab for chaining
     */
    ITab setContentSlots(@NotNull List<Integer> slots);

    /**
     * Varargs variant of {@link #setContentSlots(List)}.
     * @param slots the content slots
     * @return this tab for chaining
     */
    default ITab setContentSlots(@NotNull Integer... slots) {
        return this.setContentSlots(List.of(slots));
    }

    /**
     * Appends a single slot to the content slots.
     * @param slot the content slot to add
     * @return this tab for chaining
     */
    ITab addContentSlot(int slot);

    /**
     * Appends multiple slots to the content slots.
     * @param slots the content slots to add
     * @return this tab for chaining
     */
    ITab addContentSlots(@NotNull Collection<Integer> slots);

    /**
     * Varargs variant of {@link #addContentSlots(Collection)}.
     * @param slots the content slots to add
     * @return this tab for chaining
     */
    default ITab addContentSlots(@NotNull Integer... slots) {
        return this.addContentSlots(List.of(slots));
    }

    /**
     * Returns the current content mapping (slot index → element).
     * @return the content elements
     */
    @Contract(pure = true)
    Map<Integer, IGuiElement> getContentElements();

    /**
     * Places an element at a specific content slot, replacing anything already there.
     * @param slot the content slot index
     * @param element the element to place
     * @return this tab for chaining
     */
    ITab setContentElement(int slot, @NotNull IGuiElement element);

    /**
     * Replaces the entire content mapping with the given slot → element map.
     * @param elements the new content mapping
     * @return this tab for chaining
     */
    ITab setContentElements(@NotNull Map<Integer, IGuiElement> elements);

    /**
     * Places the element in the next free content slot (see {@link #nextFreeContentSlot()}). If the
     * tab is full the element is not added and an error is logged.
     * @param element the element to add
     * @return this tab for chaining
     */
    default ITab addContentElement(@NotNull IGuiElement element) {
        int slot = nextFreeContentSlot();
        if (slot == -1) {
            WoodLib.logger().error("Tab has no free content slot for {}", element);
            return this;
        }
        return setContentElement(slot, element);
    }

    /**
     * Adds each element to the next free content slot, in order.
     * @param elements the elements to add
     * @return this tab for chaining
     */
    default ITab addContentElements(@NotNull Collection<? extends IGuiElement> elements) {
        ITab tab = this;
        for (IGuiElement element : elements) tab = tab.addContentElement(element);
        return tab;
    }

    /**
     * Varargs variant of {@link #addContentElements(Collection)}.
     * @param elements the elements to add
     * @return this tab for chaining
     */
    default ITab addContentElements(@NotNull IGuiElement... elements) {
        return this.addContentElements(List.of(elements));
    }

    /**
     * Removes the given element from the content, wherever it sits.
     * @param element the element to remove
     * @return this tab for chaining
     */
    ITab removeContentElement(@NotNull IGuiElement element);

    /**
     * Removes the given elements from the content.
     * @param elements the elements to remove
     * @return this tab for chaining
     */
    ITab removeContentElements(@NotNull Collection<? extends IGuiElement> elements);

    /**
     * Varargs variant of {@link #removeContentElements(Collection)}.
     * @param elements the elements to remove
     * @return this tab for chaining
     */
    default ITab removeContentElements(@NotNull IGuiElement... elements) {
        return this.removeContentElements(List.of(elements));
    }

    /**
     * Whether at least one content slot is still free.
     * @return {@code true} if a free content slot exists
     */
    @Contract(pure = true)
    default boolean hasFreeContentSlot() {
        return nextFreeContentSlot() != -1;
    }

    /**
     * Finds the first content slot not yet holding an element.
     * @return the first free content slot index, or {@code -1} if all content slots are occupied
     */
    @Contract(pure = true)
    default int nextFreeContentSlot() {
        for (int slot : getContentSlots()) {
            if (!getContentElements().containsKey(slot)) return slot;
        }
        return -1;
    }

    /**
     * Renders this tab's content into the given GUI: clears the content slots, then places each
     * content element at its slot.
     * @param gui the GUI to render into (the owning tabbed GUI)
     */
    default void populateContents(@NotNull ITabbedGui<?> gui) {
        clearContentSlots(gui);
        for (int slot : getContentSlots()) {
            gui.setSlot(slot, getContentElements().getOrDefault(slot, GuiElement.empty()));
        }
    }

    /**
     * Empties this tab's content slots in the given GUI.
     * @param gui the GUI to clear the content slots in
     */
    default void clearContentSlots(@NotNull ITabbedGui<?> gui) {
        getContentSlots().forEach(slot -> gui.setSlot(slot, GuiElement.empty()));
    }
}
