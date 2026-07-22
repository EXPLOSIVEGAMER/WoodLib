package at.woodexplosive.woodlib.gui.gui;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.element.ITab;
import at.woodexplosive.woodlib.api.gui.event.GuiPageChangeEvent;
import at.woodexplosive.woodlib.api.gui.event.GuiTabChangeEvent;
import at.woodexplosive.woodlib.api.gui.event.GuiTickEvent;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import at.woodexplosive.woodlib.api.gui.gui.IPagedGui;
import at.woodexplosive.woodlib.api.gui.gui.ITabbedGui;
import at.woodexplosive.woodlib.api.gui.gui.builder.IPagedGuiBuilder;
import at.woodexplosive.woodlib.api.gui.gui.builder.ITabbedGuiBuilder;
import at.woodexplosive.woodlib.gui.element.GuiElementBuilder;
import at.woodexplosive.woodlib.gui.element.PagedTab;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A GUI that is both tabbed and paged: tab buttons switch the active {@link PagedTab}, whose own list
 * of page elements is paged across the content slots.
 *
 * <p>Tabs added to this GUI should be {@link PagedTab}s — each carries its own ordered page-element
 * list. {@link IPagedGui} operations act on the <b>active</b> tab's list, and switching tabs resets
 * to page {@code 0}.</p>
 */
public class SimpleTabbedPagedGui extends AbstractGui<SimpleTabbedPagedGui>
        implements ITabbedGui<SimpleTabbedPagedGui>, IPagedGui<SimpleTabbedPagedGui> {

    private final List<ITab> tabs = new ArrayList<>();
    private final List<Integer> tabSlots;
    private final List<Integer> pageSlots;

    protected final Callback<GuiPageChangeEvent<SimpleTabbedPagedGui>> onPageChange;
    protected final Callback<GuiTabChangeEvent<SimpleTabbedPagedGui>> onTabChange;

    private ITab activeTab;
    private int page;

    protected SimpleTabbedPagedGui(Component title, int size, InventoryType type,
                                   Callback<InventoryCloseEvent> onClose, Callback<InventoryOpenEvent> onOpen,
                                   Callback<InventoryDragEvent> onDrag, Callback<GuiTickEvent<SimpleTabbedPagedGui>> onTick,
                                   IGuiElement.ClickCallback onClickGlobal,
                                   Callback<GuiPageChangeEvent<SimpleTabbedPagedGui>> onPageChange,
                                   Callback<GuiTabChangeEvent<SimpleTabbedPagedGui>> onTabChange,
                                   boolean playerManipulation, List<Integer> tabSlots, List<Integer> pageSlots) {

        super(title, size, type, onClose, onOpen, onDrag, onTick, onClickGlobal, playerManipulation);
        this.onPageChange = onPageChange;
        this.onTabChange = onTabChange;
        this.tabSlots = tabSlots;
        this.pageSlots = pageSlots;
    }

    /**
     * Starts a builder for a tabbed, paged GUI of the given title and size.
     * @param title the inventory title
     * @param size the inventory size (multiple of 9)
     * @return a new {@link Builder}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static Builder builder(Component title, int size) {
        return new Builder(title, size);
    }

    /**
     * Starts a builder for a tabbed, paged GUI of the given title and {@link InventoryType}.
     * @param title the inventory title
     * @param type the inventory type (its default size is used)
     * @return a new {@link Builder}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static Builder builder(Component title, @NotNull InventoryType type) {
        return new Builder(title, type);
    }

    // ---- Tabbed ----

    @Override
    public @NonNull List<ITab> getTabs() {
        return this.tabs;
    }

    @Override
    public SimpleTabbedPagedGui addTab(@NonNull ITab tab) {
        if (!(tab instanceof PagedTab)) {
            WoodLib.logger().warn("SimpleTabbedPagedGui expects a PagedTab but got {} - its content will not be paged",
                    tab.getClass().getSimpleName());
        }
        this.tabs.add(tab);
        if (this.activeTab == null) this.activeTab = tab;
        return this;
    }

    @Override
    public SimpleTabbedPagedGui addTabs(@NonNull Collection<? extends ITab> tabs) {
        for (ITab tab : tabs) this.addTab(tab);
        return this;
    }

    @Override
    public ITab getTab() {
        return this.activeTab;
    }

    @Override
    public SimpleTabbedPagedGui setTab(@NonNull ITab tab) {
        GuiTabChangeEvent<SimpleTabbedPagedGui> event = new GuiTabChangeEvent<>(this, this.activeTab, tab);
        if (event.callEvent() && !this.onTabChange.run(event)) {
            this.activeTab = tab;
            this.page = 0;
            this.redraw();
        }
        return this;
    }

    @Override
    public @NonNull List<Integer> getTabSlots() {
        return this.tabSlots;
    }

    @Override
    public @NonNull List<Integer> getContentSlots() {
        return this.pageSlots;
    }

    // ---- Paged (acts on the active tab's own element list) ----

    @Override
    public @NonNull LinkedList<IGuiElement> getPageElements() {
        return this.activeTab instanceof PagedTab pagedTab ? pagedTab.getPageElements() : new LinkedList<>();
    }

    @Override
    public SimpleTabbedPagedGui addPageElement(@NonNull IGuiElement element) {
        if (this.activeTab instanceof PagedTab pagedTab) pagedTab.addPageElement(element);
        return this;
    }

    @Override
    public SimpleTabbedPagedGui addPageElements(@NonNull Collection<? extends IGuiElement> elements) {
        if (this.activeTab instanceof PagedTab pagedTab) pagedTab.addPageElements(elements);
        return this;
    }

    @Override
    public SimpleTabbedPagedGui removePageElement(@NonNull IGuiElement element) {
        if (this.activeTab instanceof PagedTab pagedTab) pagedTab.removePageElement(element);
        return this;
    }

    @Override
    public SimpleTabbedPagedGui removePageElements(@NonNull Collection<? extends IGuiElement> elements) {
        if (this.activeTab instanceof PagedTab pagedTab) pagedTab.removePageElements(elements);
        return this;
    }

    @Override
    public SimpleTabbedPagedGui setPageElement(@NonNull LinkedList<? extends IGuiElement> elements) {
        if (this.activeTab instanceof PagedTab pagedTab) pagedTab.setPageElements(elements);
        return this;
    }

    @Override
    public int getPage() {
        return this.page;
    }

    @Override
    public SimpleTabbedPagedGui setPage(int page) {
        GuiPageChangeEvent<SimpleTabbedPagedGui> event = new GuiPageChangeEvent<>(this, getMaxPage(), this.page, page);
        if (event.callEvent() && !this.onPageChange.run(event)) {
            this.page = page;
            this.redraw();
        }
        return this;
    }

    @Override
    public @NonNull List<Integer> getPageSlots() {
        return this.pageSlots;
    }

    @Override
    public SimpleTabbedPagedGui setNextPageElement(int slot, @NonNull IGuiElement element) {
        if (!element.hasCallback()) element = GuiElementBuilder.of(element).setCallback((event, clickedGui, clickedElement, clickType, action) -> {
            this.nextPage();
            return true;
        }).buildElement();

        this.setSlot(slot, element);
        return this;
    }

    @Override
    public SimpleTabbedPagedGui setPreviousPageElement(int slot, @NonNull IGuiElement element) {
        if (!element.hasCallback()) element = GuiElementBuilder.of(element).setCallback((event, clickedGui, clickedElement, clickType, action) -> {
            this.previousPage();
            return true;
        }).buildElement();

        this.setSlot(slot, element);
        return this;
    }

    @Override
    public @Nullable InventoryView open(@NonNull Player player) {
        this.populateTabs();
        this.populatePage();
        return super.open(player);
    }

    // ---- Builder ----

    /** Fluent builder for {@link SimpleTabbedPagedGui}. */
    public static class Builder implements IPagedGuiBuilder<Builder, SimpleTabbedPagedGui>, ITabbedGuiBuilder<Builder, SimpleTabbedPagedGui> {
        private final int size;
        private final Component title;
        private final InventoryType type;

        private List<Integer> tabSlots = new ArrayList<>();
        private List<Integer> pageSlots = new ArrayList<>();
        private final List<ITab> tabs = new ArrayList<>();
        private boolean playerManipulation = false;
        private Callback<InventoryCloseEvent> onClose = IGui.emptyCallback();
        private Callback<InventoryOpenEvent> onOpen = IGui.emptyCallback();
        private Callback<InventoryDragEvent> onDrag = IGui.emptyCallback();
        private Callback<GuiTickEvent<SimpleTabbedPagedGui>> onTick = IGui.emptyCallback();
        private Callback<GuiPageChangeEvent<SimpleTabbedPagedGui>> onPageChange = IGui.emptyCallback();
        private Callback<GuiTabChangeEvent<SimpleTabbedPagedGui>> onTabChange = IGui.emptyCallback();
        private IGuiElement.ClickCallback onClickGlobal = IGuiElement.EMPTY_CALLBACK;

        public Builder(Component title, int size) {
            this.title = title;
            this.size = size;
            this.type = null;
        }

        public Builder(Component title, InventoryType type) {
            this.title = title;
            this.size = type.getDefaultSize();
            this.type = type;
        }

        @Override
        public Builder setOnClose(@NotNull Callback<InventoryCloseEvent> onClose) {
            this.onClose = onClose;
            return this;
        }

        @Override
        public Builder setOnOpen(@NotNull Callback<InventoryOpenEvent> onOpen) {
            this.onOpen = onOpen;
            return this;
        }

        @Override
        public Builder setOnDrag(@NotNull Callback<InventoryDragEvent> onDrag) {
            this.onDrag = onDrag;
            return this;
        }

        @Override
        public Builder setOnTick(@NotNull Callback<GuiTickEvent<SimpleTabbedPagedGui>> onTick) {
            this.onTick = onTick;
            return this;
        }

        @Override
        public Builder setOnClickGlobal(@NotNull IGuiElement.ClickCallback onClickGlobal) {
            this.onClickGlobal = onClickGlobal;
            return this;
        }

        @Override
        public Builder setPlayerManipulation(boolean playerManipulation) {
            this.playerManipulation = playerManipulation;
            return this;
        }

        @Override
        public Builder setTabSlots(@NonNull List<Integer> slots) {
            this.tabSlots = new ArrayList<>(slots);
            return this;
        }

        @Override
        public Builder addTabSlot(Integer slot) {
            this.tabSlots.add(slot);
            return this;
        }

        @Override
        public Builder addTabSlots(@NonNull Collection<Integer> slots) {
            this.tabSlots.addAll(slots);
            return this;
        }

        @Override
        public Builder removeTabSlot(Integer slot) {
            this.tabSlots.remove(slot);
            return this;
        }

        @Override
        public Builder removeTabSlots(@NonNull Collection<Integer> slots) {
            this.tabSlots.removeAll(slots);
            return this;
        }

        @Override
        public Builder setContentSlots(@NonNull List<Integer> slots) {
            this.pageSlots = new ArrayList<>(slots);
            return this;
        }

        @Override
        public Builder addContentSlot(Integer slot) {
            this.pageSlots.add(slot);
            return this;
        }

        @Override
        public Builder addContentSlots(@NonNull Collection<Integer> slots) {
            this.pageSlots.addAll(slots);
            return this;
        }

        @Override
        public Builder removeContentSlot(Integer slot) {
            this.pageSlots.remove(slot);
            return this;
        }

        @Override
        public Builder removeContentSlots(@NonNull Collection<Integer> slots) {
            this.pageSlots.removeAll(slots);
            return this;
        }

        @Override
        public Builder setPageSlots(@NotNull List<Integer> slots) {
            this.pageSlots = new ArrayList<>(slots);
            return this;
        }

        @Override
        public Builder addPageSlot(Integer slot) {
            this.pageSlots.add(slot);
            return this;
        }

        @Override
        public Builder addPageSlots(@NonNull Collection<Integer> slots) {
            this.pageSlots.addAll(slots);
            return this;
        }

        @Override
        public Builder removePageSlot(Integer slot) {
            this.pageSlots.remove(slot);
            return this;
        }

        @Override
        public Builder removePageSlots(@NonNull Collection<Integer> slots) {
            this.pageSlots.removeAll(slots);
            return this;
        }

        @Override
        public Builder addTab(@NonNull ITab tab) {
            this.tabs.add(tab);
            return this;
        }

        /**
         * Adds a paged tab together with its page elements.
         * @param tab the tab to add
         * @param pageElements the tab's page elements
         * @return this builder for chaining
         */
        public Builder addTab(PagedTab tab, Collection<? extends IGuiElement> pageElements) {
            tab.addPageElements(pageElements);
            this.tabs.add(tab);
            return this;
        }

        @Override
        public Builder onPageChange(@NotNull Callback<GuiPageChangeEvent<SimpleTabbedPagedGui>> onPageChange) {
            this.onPageChange = onPageChange;
            return this;
        }

        @Override
        public Builder onTabChange(@NotNull Callback<GuiTabChangeEvent<SimpleTabbedPagedGui>> onTabChange) {
            this.onTabChange = onTabChange;
            return this;
        }

        @Override
        public @NonNull SimpleTabbedPagedGui build() {
            SimpleTabbedPagedGui gui = new SimpleTabbedPagedGui(title, size, type, onClose, onOpen, onDrag, onTick,
                    onClickGlobal, onPageChange, onTabChange, playerManipulation, tabSlots, pageSlots);
            for (ITab tab : this.tabs) gui.addTab(tab);
            return gui;
        }
    }
}
