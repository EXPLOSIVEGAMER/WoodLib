package at.woodexplosive.woodlib.gui.gui;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.element.ITab;
import at.woodexplosive.woodlib.api.gui.event.GuiTabChangeEvent;
import at.woodexplosive.woodlib.api.gui.event.GuiTickEvent;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import at.woodexplosive.woodlib.api.gui.gui.ITabbedGui;
import at.woodexplosive.woodlib.api.gui.gui.builder.ITabbedGuiBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A tabbed {@link ITabbedGui}: tab buttons in the tab slots switch which {@link ITab}'s content is
 * shown in the content slots. Create one through {@link #builder(Component, int)}, add tabs, then
 * open it. Opening (re)renders the tab buttons and the active tab's content.
 */
public class SimpleTabbedGui extends AbstractGui<SimpleTabbedGui> implements ITabbedGui<SimpleTabbedGui> {

    private final List<ITab> tabs = new ArrayList<>();
    private final List<Integer> tabSlots;
    private final List<Integer> contentSlots;

    protected final Callback<GuiTabChangeEvent<SimpleTabbedGui>> onTabChange;

    private ITab activeTab;

    protected SimpleTabbedGui(Component title, int size, InventoryType type,
                             Callback<InventoryCloseEvent> onClose, Callback<InventoryOpenEvent> onOpen,
                             Callback<InventoryDragEvent> onDrag, Callback<GuiTickEvent<SimpleTabbedGui>> onTick,
                             IGuiElement.ClickCallback onClickGlobal,
                             Callback<GuiTabChangeEvent<SimpleTabbedGui>> onTabChange,
                             boolean playerManipulation, List<Integer> tabSlots, List<Integer> contentSlots) {

        super(title, size, type, onClose, onOpen, onDrag, onTick, onClickGlobal, playerManipulation);
        this.onTabChange = onTabChange;
        this.tabSlots = tabSlots;
        this.contentSlots = contentSlots;
    }

    /**
     * Starts a builder for a tabbed GUI of the given title and size.
     * @param title the inventory title
     * @param size the inventory size (multiple of 9)
     * @return a new {@link Builder}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static Builder builder(Component title, int size) {
        return new Builder(title, size);
    }

    /**
     * Starts a builder for a tabbed GUI of the given title and {@link InventoryType}.
     * @param title the inventory title
     * @param type the inventory type (its default size is used)
     * @return a new {@link Builder}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static Builder builder(Component title, @NotNull InventoryType type) {
        return new Builder(title, type);
    }

    @Override
    public List<ITab> getTabs() {
        return this.tabs;
    }

    @Override
    public SimpleTabbedGui addTab(ITab tab) {
        tab.setContentSlots(this.contentSlots);
        this.tabs.add(tab);
        if (this.activeTab == null) this.activeTab = tab;
        return this;
    }

    @Override
    public SimpleTabbedGui addTabs(Collection<? extends ITab> tabs) {
        for (ITab tab : tabs) this.addTab(tab);
        return this;
    }

    @Override
    public ITab getTab() {
        return this.activeTab;
    }

    @Override
    public SimpleTabbedGui setTab(ITab tab) {
        GuiTabChangeEvent<SimpleTabbedGui> event = new GuiTabChangeEvent<>(this, this.activeTab, tab);
        if (event.callEvent() && !this.onTabChange.run(event)) {
            this.activeTab = tab;
            this.redraw();
        }
        return this;
    }

    @Override
    public List<Integer> getTabSlots() {
        return this.tabSlots;
    }

    @Override
    public List<Integer> getContentSlots() {
        return this.contentSlots;
    }

    @Override
    public InventoryView open(Player player) {
        this.populateTabs();
        this.populateContent();
        return super.open(player);
    }

    // Builder

    /** Fluent builder for {@link SimpleTabbedGui}. */
    public static class Builder implements ITabbedGuiBuilder<Builder, SimpleTabbedGui> {
        private final int size;
        private final Component title;
        private final InventoryType type;

        private List<Integer> tabSlots = new ArrayList<>();
        private List<Integer> contentSlots = new ArrayList<>();
        private final List<ITab> tabs = new ArrayList<>();
        private boolean playerManipulation = false;
        private Callback<InventoryCloseEvent> onClose = IGui.emptyCallback();
        private Callback<InventoryOpenEvent> onOpen = IGui.emptyCallback();
        private Callback<InventoryDragEvent> onDrag = IGui.emptyCallback();
        private Callback<GuiTickEvent<SimpleTabbedGui>> onTick = IGui.emptyCallback();
        private Callback<GuiTabChangeEvent<SimpleTabbedGui>> onTabChange = IGui.emptyCallback();
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
        public Builder setOnTick(@NotNull Callback<GuiTickEvent<SimpleTabbedGui>> onTick) {
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
        public Builder setContentSlots(List<Integer> slots) {
            this.contentSlots = slots;
            return this;
        }

        @Override
        public Builder addContentSlot(int slot) {
            this.contentSlots.add(slot);
            return this;
        }

        @Override
        public Builder addContentSlots(Collection<Integer> slots) {
            this.contentSlots.addAll(slots);
            return this;
        }

        @Override
        public Builder setTabSlots(List<Integer> slots) {
            this.tabSlots = slots;
            return this;
        }

        @Override
        public Builder addTabSlot(int slot) {
            this.tabSlots.add(slot);
            return this;
        }

        @Override
        public Builder addTabSlots(Collection<Integer> slots) {
            this.tabSlots.addAll(slots);
            return this;
        }

        @Override
        public Builder addTab(ITab tab) {
            this.tabs.add(tab);
            return this;
        }

        @Override
        public Builder onTabChange(@NotNull Callback<GuiTabChangeEvent<SimpleTabbedGui>> onTabChange) {
            this.onTabChange = onTabChange;
            return this;
        }

        @Override
        public SimpleTabbedGui build() {
            SimpleTabbedGui gui = new SimpleTabbedGui(title, size, type, onClose, onOpen, onDrag, onTick,
                    onClickGlobal, onTabChange, playerManipulation, tabSlots, contentSlots);
            for (ITab tab : this.tabs) gui.addTab(tab);
            return gui;
        }
    }
}
