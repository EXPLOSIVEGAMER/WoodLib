package at.woodexplosive.woodlib.gui.gui;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.element.ITab;
import at.woodexplosive.woodlib.api.gui.event.*;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import at.woodexplosive.woodlib.api.gui.gui.IPagedGui;
import at.woodexplosive.woodlib.api.gui.gui.ITabbedGui;
import at.woodexplosive.woodlib.api.gui.gui.builder.IPagedGuiBuilder;
import at.woodexplosive.woodlib.api.gui.gui.builder.ITabbedGuiBuilder;
import at.woodexplosive.woodlib.gui.element.GuiElementBuilder;
import at.woodexplosive.woodlib.gui.element.PagedTab;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
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

    /**
     * The tabs of this GUI, in order.
     */
    private final List<ITab> tabs = new ArrayList<>();
    /**
     * The slot indices the tab buttons are rendered into.
     */
    private final List<Integer> tabSlots;
    /**
     * The slot indices the active tab's page content is rendered into.
     */
    private final List<Integer> pageSlots;

    /**
     * Callback run when the page changes; returning {@code true} cancels the change.
     */
    protected final Callback<GuiPageChangeEvent> onPageChange;
    /**
     * Callback run when the active tab changes; returning {@code true} cancels the change.
     */
    protected final Callback<GuiTabChangeEvent> onTabChange;

    /**
     * The currently active tab, or {@code null} if no tab has been added yet.
     */
    private ITab activeTab;
    /**
     * The current (0-based) page index within the active tab.
     */
    private int page;

    /**
     * @param title              the inventory title
     * @param size               the inventory size (multiple of 9); ignored if {@code type} is non-null
     * @param type               the inventory type, or {@code null} to create a plain chest inventory of {@code size}
     * @param onClose            the close callback
     * @param onOpen             the open callback
     * @param onDrag             the drag callback
     * @param onTick             the per-tick callback
     * @param onClickGlobal      the global click callback
     * @param onPageChange       the page-change callback
     * @param onTabChange        the tab-change callback
     * @param playerManipulation {@code true} to allow the player to move items in the inventory
     * @param tabSlots           the slot indices the tab buttons are rendered into
     * @param pageSlots          the slot indices the active tab's page content is rendered into
     * @param parent             the parent Gui can be null if there's none
     */
    private SimpleTabbedPagedGui(@NotNull Component title, int size, @Nullable InventoryType type,
                                 @NotNull Callback<GuiCloseEvent> onClose, @NotNull Callback<GuiOpenEvent> onOpen,
                                 @NotNull Callback<GuiInteractEvent> onInteract,
                                 @NotNull Callback<GuiDragEvent> onDrag, @NotNull Callback<GuiTickEvent> onTick,
                                 IGuiElement.@NotNull ClickCallback onClickGlobal,
                                 @NotNull Callback<GuiPageChangeEvent> onPageChange,
                                 @NotNull Callback<GuiTabChangeEvent> onTabChange,
                                 boolean playerManipulation, @NotNull List<Integer> tabSlots, @NotNull List<Integer> pageSlots,
                                 @Nullable IGui<?> parent
    ) {

        super(title, size, type, onClose, onOpen, onInteract, onDrag, onTick, onClickGlobal, playerManipulation, parent);
        this.onPageChange = onPageChange;
        this.onTabChange = onTabChange;
        this.tabSlots = tabSlots;
        this.pageSlots = pageSlots;
    }

    /**
     * Template-method constructor for subclasses: builds the inventory from the {@link AbstractGui}
     * hooks plus {@link #tabSlots()}, {@link #pageSlots()}, {@link #onPageChange(GuiPageChangeEvent)}
     * and {@link #onTabChange(GuiTabChangeEvent)}, then runs {@link #init()}.
     */
    protected SimpleTabbedPagedGui() {
        super();
        this.tabSlots = tabSlots();
        this.pageSlots = pageSlots();
        this.onPageChange = this::onPageChange;
        this.onTabChange = this::onTabChange;

        this.init();
    }

    /**
     * The slot indices the tab buttons are rendered into. Overridden by no-arg-constructor
     * subclasses; defaults to no tab slots.
     * @return the tab slots
     */
    protected @NotNull List<Integer> tabSlots() {
        return List.of();
    }

    /**
     * The slot indices the active tab's page content is rendered into. Overridden by
     * no-arg-constructor subclasses; defaults to no page slots.
     * @return the page slots
     */
    protected @NotNull List<Integer> pageSlots() {
        return List.of();
    }

    /**
     * Hook run on {@link GuiPageChangeEvent} (wired as this GUI's page-change callback). No-op by
     * default.
     * @param event the page-change event
     * @return {@code true} to cancel the page change
     */
    protected boolean onPageChange(@NotNull GuiPageChangeEvent event) {
        return false;
    }

    /**
     * Hook run on {@link GuiTabChangeEvent} (wired as this GUI's tab-change callback). No-op by
     * default.
     * @param event the tab-change event
     * @return {@code true} to cancel the tab change
     */
    protected boolean onTabChange(@NotNull GuiTabChangeEvent event) {
        return false;
    }

    /**
     * Starts a builder for a tabbed and paged GUI of the given title and size.
     *
     * @param title  the inventory title
     * @param size   the inventory size (multiple of 9)
     * @param parent the parent Gui can be null if there's none
     * @return a new {@link Builder}
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static Builder builder(Component title, int size, @Nullable IGui<?> parent) {
        return new Builder(title, size, parent);
    }

    /**
     * Starts a builder for a tabbed and paged GUI of the given title and {@link InventoryType}.
     *
     * @param title  the inventory title
     * @param type   the inventory type (its default size is used)
     * @param parent the parent Gui can be null if there's none
     * @return a new {@link Builder}
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static Builder builder(Component title, @NotNull InventoryType type, @Nullable IGui<?> parent) {
        return new Builder(title, type, parent);
    }

    // ---- Tabbed ----

    @Override
    public @Nullable InventoryView open(@NotNull Player player, @Nullable ITab tab) {
        return this.open(player, tab, 0);
    }

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
        GuiTabChangeEvent event = new GuiTabChangeEvent(this, this.activeTab, tab);
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
    public @Nullable InventoryView open(@NotNull Player player, int page) {
        return this.open(player, this.activeTab, page);
    }

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
        GuiPageChangeEvent event = new GuiPageChangeEvent(this, getMaxPage(), this.page, page);
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
        if (!element.hasCallback()) element = GuiElementBuilder.of(element).setCallback(event -> {
            this.nextPage();
            return true;
        }).buildElement();

        this.setSlot(slot, element);
        return this;
    }

    @Override
    public SimpleTabbedPagedGui setPreviousPageElement(int slot, @NonNull IGuiElement element) {
        if (!element.hasCallback()) element = GuiElementBuilder.of(element).setCallback(event -> {
            this.previousPage();
            return true;
        }).buildElement();

        this.setSlot(slot, element);
        return this;
    }

    // Paged Tabbed

    /**
     * Disambiguates the {@code firstTrueEmpty()} default inherited from both {@link ITabbedGui} and
     * {@link IPagedGui} in favor of {@link ITabbedGui}'s (tab + content slots), since content and page
     * slots are the same here.
     * @return first empty slot index outside the tab/content slots, otherwise -1
     */
    @Override
    public int firstTrueEmpty() {
        return ITabbedGui.super.firstTrueEmpty();
    }

    @Override
    public SimpleTabbedPagedGui addSlot(@NotNull IGuiElement element) {
        int slot = this.firstTrueEmpty();
        if (slot != -1) {
            this.setSlot(slot, element);
        } else {
            WoodLib.logger().error("There are no more slots empty in {}!", this);
        }
        return this;
    }

    /**
     * Opens the inventory for {@code player} with the given tab active on the given page.
     * @param player the player
     * @param tab the tab to activate, or {@code null} to keep the current tab
     * @param page the page to open the tab on
     * @return the newly opened {@link InventoryView}
     */
    public @Nullable InventoryView open(@NonNull Player player, @Nullable ITab tab, int page) {
        this.activeTab = tab;
        this.page = page;
        this.populateTabs();
        this.populatePage();
        return super.open(player);
    }

    @Override
    public @Nullable InventoryView open(@NonNull Player player) {
        return this.open(player, this.activeTab, 0);
    }

    @Override
    public void redraw() {
        if (this.player == null) return;
        this.open(this.player, this.activeTab, this.page);
    }

    // ---- Builder ----

    /**
     * Fluent builder for {@link SimpleTabbedPagedGui}.
     */
    public static class Builder implements IPagedGuiBuilder<Builder, SimpleTabbedPagedGui>, ITabbedGuiBuilder<Builder, SimpleTabbedPagedGui> {
        private final int size;
        private final Component title;
        private final @Nullable IGui<?> parent;
        private final InventoryType type;

        private List<Integer> tabSlots = new ArrayList<>();
        private List<Integer> pageSlots = new ArrayList<>();
        private final List<ITab> tabs = new ArrayList<>();
        private boolean playerManipulation = false;
        private Callback<GuiCloseEvent> onClose = IGui.emptyCallback();
        private Callback<GuiOpenEvent> onOpen = IGui.emptyCallback();
        private Callback<GuiInteractEvent> onInteract = IGui.emptyCallback();
        private Callback<GuiDragEvent> onDrag = IGui.emptyCallback();
        private Callback<GuiTickEvent> onTick = IGui.emptyCallback();
        private Callback<GuiPageChangeEvent> onPageChange = IGui.emptyCallback();
        private Callback<GuiTabChangeEvent> onTabChange = IGui.emptyCallback();
        private IGuiElement.ClickCallback onClickGlobal = IGuiElement.EMPTY_CALLBACK;

        /**
         * @param title  the inventory title
         * @param size   the inventory size (multiple of 9)
         * @param parent the parent GUI set to null if there's none
         */
        public Builder(Component title, int size, @Nullable IGui<?> parent) {
            this.title = title;
            this.size = size;
            this.type = null;
            this.parent = parent;
        }

        /**
         * @param title  the inventory title
         * @param type   the inventory type (its default size is used)
         * @param parent the parent GUI set to null if there's none
         */
        public Builder(Component title, InventoryType type, @Nullable IGui<?> parent) {
            this.title = title;
            this.size = type.getDefaultSize();
            this.type = type;
            this.parent = parent;
        }

        @Override
        public Builder setOnClose(@NotNull Callback<GuiCloseEvent> onClose) {
            this.onClose = onClose;
            return this;
        }

        @Override
        public Builder setOnOpen(@NotNull Callback<GuiOpenEvent> onOpen) {
            this.onOpen = onOpen;
            return this;
        }

        @Override
        public Builder setOnInteract(@NotNull IGui.Callback<GuiInteractEvent> onInteract) {
            this.onInteract = onInteract;
            return this;
        }

        @Override
        public Builder setOnDrag(@NotNull Callback<GuiDragEvent> onDrag) {
            this.onDrag = onDrag;
            return this;
        }

        @Override
        public Builder setOnTick(@NotNull Callback<GuiTickEvent> onTick) {
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
         *
         * @param tab          the tab to add
         * @param pageElements the tab's page elements
         * @return this builder for chaining
         */
        public Builder addTab(PagedTab tab, Collection<? extends IGuiElement> pageElements) {
            tab.addPageElements(pageElements);
            this.tabs.add(tab);
            return this;
        }

        @Override
        public Builder setOnPageChange(@NotNull Callback<GuiPageChangeEvent> onPageChange) {
            this.onPageChange = onPageChange;
            return this;
        }

        @Override
        public Builder setOnTabChange(@NotNull Callback<GuiTabChangeEvent> onTabChange) {
            this.onTabChange = onTabChange;
            return this;
        }

        @Override
        public @NonNull SimpleTabbedPagedGui build() {
            SimpleTabbedPagedGui gui = new SimpleTabbedPagedGui(title, size, type, onClose, onOpen, onInteract, onDrag, onTick,
                    onClickGlobal, onPageChange, onTabChange, playerManipulation, tabSlots, pageSlots,
                    parent);
            for (ITab tab : this.tabs) gui.addTab(tab);
            return gui;
        }
    }
}
