package at.woodexplosive.woodlib.api.gui.gui;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.element.ITab;
import at.woodexplosive.woodlib.gui.element.GuiElementBuilder;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.List;

/**
 * An {@link IGui} organized into {@link ITab tabs}. A row of tab buttons (rendered into the
 * {@link #getTabSlots() tab slots}) switches which tab is active; the active tab's content is drawn
 * into the {@link #getContentSlots() content slots}.
 *
 * <p>Switching is done with {@link #setTab(ITab)} (or {@link #nextTab()} / {@link #previousTab()}),
 * which fires a {@link at.woodexplosive.woodlib.api.gui.event.GuiTabChangeEvent}. Tab buttons that
 * carry no click callback of their own automatically get one that selects their tab.</p>
 *
 * @param <T> the concrete tabbed-GUI type, for fluent self-returning methods (CRTP)
 */
public interface ITabbedGui<T extends ITabbedGui<T>> extends IGui<T> {

    /**
     * Returns the tabs of this GUI, in order.
     * @return the tabs
     */
    @Contract(pure = true)
    List<ITab> getTabs();

    /**
     * Appends a tab. The GUI's {@link #getContentSlots() content slots} are assigned to it, and it
     * becomes the active tab if none was active yet.
     * @param tab the tab to add
     * @return this GUI for chaining
     */
    T addTab(ITab tab);

    /**
     * Appends multiple tabs.
     * @param tabs the tabs to add
     * @return this GUI for chaining
     */
    T addTabs(Collection<? extends ITab> tabs);

    /**
     * Varargs variant of {@link #addTabs(Collection)}.
     * @param tabs the tabs to add
     * @return this GUI for chaining
     */
    default T addTabs(ITab... tabs) {
        return this.addTabs(List.of(tabs));
    }

    /**
     * Returns the currently active tab.
     * @return the active tab, or {@code null} if the GUI has no tabs
     */
    @Contract(pure = true)
    ITab getTab();

    /**
     * Switches to the given tab (fires a
     * {@link at.woodexplosive.woodlib.api.gui.event.GuiTabChangeEvent}).
     * @param tab the tab to activate
     * @return this GUI for chaining
     */
    T setTab(ITab tab);

    /**
     * Returns the slot indices the tab buttons are rendered into.
     * @return the tab slots
     */
    @Contract(pure = true)
    List<Integer> getTabSlots();

    /**
     * Returns the slot indices the active tab's content is rendered into.
     * @return the content slots
     */
    @Contract(pure = true)
    List<Integer> getContentSlots();

    /**
     * Returns the index of the active tab within {@link #getTabs()}.
     * @return the active tab index, or {@code -1} if there is no active tab
     */
    @Contract(pure = true)
    default int getTabIndex() {
        return this.getTabs().indexOf(this.getTab());
    }

    /** Switches to the next tab, clamped to the last tab. */
    default void nextTab() {
        int i = this.getTabIndex();
        if (i < 0) return;
        this.setTab(this.getTabs().get(Math.min(this.getTabs().size() - 1, i + 1)));
    }

    /** Switches to the previous tab, clamped to the first tab. */
    default void previousTab() {
        int i = this.getTabIndex();
        if (i <= 0) return;
        this.setTab(this.getTabs().get(i - 1));
    }

    /**
     * Renders the tab buttons into the tab slots. A button without its own click callback gets one
     * that selects its tab.
     */
    default void populateTabs() {
        List<Integer> slots = this.getTabSlots();
        List<ITab> tabs = this.getTabs();
        for (int i = 0; i < slots.size() && i < tabs.size(); i++) {
            final ITab tab = tabs.get(i);
            IGuiElement button = tab.getTabElement();
            if (button == null) continue;
            if (!button.hasCallback()) {
                button = GuiElementBuilder.of(button).setCallback((event, gui, element, clickType, action) -> {
                    this.setTab(tab);
                    return true;
                }).buildElement();
            }
            this.setSlot(slots.get(i), button);
        }
    }

    /** Renders the active tab's content into the content slots. */
    default void populateContent() {
        ITab tab = this.getTab();
        if (tab != null) tab.populateContents(this);
    }
}
