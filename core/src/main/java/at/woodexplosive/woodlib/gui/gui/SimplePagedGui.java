package at.woodexplosive.woodlib.gui.gui;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.event.GuiPageChangeEvent;
import at.woodexplosive.woodlib.api.gui.event.GuiTickEvent;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import at.woodexplosive.woodlib.api.gui.gui.IPagedGui;
import at.woodexplosive.woodlib.api.gui.gui.Listener.IPagedGuiListener;
import at.woodexplosive.woodlib.api.gui.gui.builder.IPagedGuiBuilder;
import at.woodexplosive.woodlib.gui.element.GuiElementBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A multi-page {@link IPagedGui}: page elements are spread across the configured page slots and
 * navigated with page controls. Create one through {@link #builder(Component, int)}, add elements via
 * {@link #addPageElement(at.woodexplosive.woodlib.api.gui.element.IGuiElement)}, optionally place
 * next/previous controls, then open it. Opening (re)populates the current page automatically.
 */
public class SimplePagedGui extends AbstractGui<SimplePagedGui> implements IPagedGui<SimplePagedGui> {
    private final List<Integer> pageSlots;

    protected final Callback<GuiPageChangeEvent<SimplePagedGui>> onPageChange;

    private LinkedList<IGuiElement> pageElements = new LinkedList<>();

    private int page;

    protected SimplePagedGui(Component title, int size, InventoryType type, Callback<InventoryCloseEvent> onClose,
                             Callback<InventoryOpenEvent> onOpen, Callback<InventoryDragEvent> onDrag,
                             Callback<GuiTickEvent<SimplePagedGui>> onTick, IGuiElement.ClickCallback onClickGlobal,
                             Callback<GuiPageChangeEvent<SimplePagedGui>> onPageChange,
                             boolean playerManipulation, List<Integer> pageSlots) {

        super(title, size, type, onClose, onOpen, onDrag, onTick, onClickGlobal, playerManipulation);
        this.onPageChange = onPageChange;
        this.pageSlots = pageSlots;
    }

    /**
     * Starts a builder for a paged GUI of the given title and size. Every slot is a page slot by
     * default; narrow this with {@link Builder#setPageSlots(List)}.
     * @param title the inventory title
     * @param size the inventory size (multiple of 9)
     * @return a new {@link Builder}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static Builder builder(Component title, int size) {
        return new Builder(title, size);
    }

    /**
     * Starts a builder for a paged GUI of the given title and {@link InventoryType}.
     * @param title the inventory title
     * @param type the inventory type (its default size is used)
     * @return a new {@link Builder}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static Builder builder(Component title, @NotNull InventoryType type) {
        return new Builder(title, type);
    }

    @Override
    public SimplePagedGui setPage(int page) {
        GuiPageChangeEvent<SimplePagedGui> event = new GuiPageChangeEvent<>(this, getMaxPage(), this.page, page);
        if (event.callEvent() && !this.onPageChange.run(event)) {
            this.page = page;
            this.redraw();
        }
        return this;
    }

    @Override
    public LinkedList<IGuiElement> getPageElements() {
        return this.pageElements;
    }

    @Override
    public SimplePagedGui addPageElement(IGuiElement element) {
        this.pageElements.add(element);
        return this;
    }

    @Override
    public SimplePagedGui addPageElements(Collection<? extends IGuiElement> elements) {
        this.pageElements.addAll(elements);
        return this;
    }

    @Override
    public SimplePagedGui removePageElement(IGuiElement element) {
        this.pageElements.remove(element);
        return this;
    }

    @Override
    public SimplePagedGui removePageElements(Collection<? extends IGuiElement> elements) {
        this.pageElements.removeAll(elements);
        return this;
    }

    @Override
    public SimplePagedGui setPageElement(LinkedList<? extends IGuiElement> elements) {
        this.pageElements = new LinkedList<>(elements);
        return this;
    }

    @Override
    public int getPage() {
        return this.page;
    }

    @Override
    public List<Integer> getPageSlots() {
        return this.pageSlots;
    }

    @Override
    public SimplePagedGui setNextPageElement(int slot, IGuiElement element) {
        if (!element.hasCallback()) element = GuiElementBuilder.of(element).setCallback((event, clickedGui, clickedElement, clickType, action) -> {
            this.nextPage();
            return true;
        }).buildElement();

        this.setSlot(slot, element);
        return this;
    }

    @Override
    public SimplePagedGui setPreviousPageElement(int slot, IGuiElement element) {
        if (!element.hasCallback()) element = GuiElementBuilder.of(element).setCallback((event, clickedGui, clickedElement, clickType, action) -> {
            this.previousPage();
            return true;
        }).buildElement();

        this.setSlot(slot, element);
        return this;
    }

    @Override
    public InventoryView open(Player player) {
        this.populatePage();
        return super.open(player);
    }

    // Builder

    /** Fluent builder for {@link SimplePagedGui}. */
    public static class Builder implements IPagedGuiBuilder<Builder, SimplePagedGui> {
        private final int size;
        private final Component title;
        private final InventoryType type;

        private List<Integer> pageSlots = new ArrayList<>();
        private boolean playerManipulation;
        private Callback<InventoryCloseEvent> onClose = IGui.emptyCallback();
        private Callback<InventoryOpenEvent> onOpen = IGui.emptyCallback();
        private Callback<InventoryDragEvent> onDrag = IGui.emptyCallback();
        private Callback<GuiTickEvent<SimplePagedGui>> onTick = IGui.emptyCallback();
        private Callback<GuiPageChangeEvent<SimplePagedGui>> onPageChange = IGui.emptyCallback();
        private IGuiElement.ClickCallback onClickGlobal = IGuiElement.EMPTY_CALLBACK;

        public Builder(Component title, int size) {
            this.title = title;
            this.size = size;
            this.type = null;

            for (int i = 0; i < size; i++) pageSlots.add(i);
        }

        public Builder(Component title, InventoryType type) {
            this.title = title;
            this.size = type.getDefaultSize();
            this.type = type;

            for (int i = 0; i < this.size; i++) pageSlots.add(i);
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
        public Builder setOnTick(@NotNull IGui.Callback<GuiTickEvent<SimplePagedGui>> onTick) {
            this.onTick = onTick;
            return this;
        }

        @Override
        public Builder setOnClickGlobal(@NotNull IGuiElement.ClickCallback onClickGlobal) {
            this.onClickGlobal = onClickGlobal;
            return this;
        }


        @Override
        public Builder onPageChange(@NotNull Callback<GuiPageChangeEvent<SimplePagedGui>> onPageChange) {
            this.onPageChange = onPageChange;
            return this;
        }

        @Override
        public Builder setPlayerManipulation(boolean playerManipulation) {
            this.playerManipulation = playerManipulation;
            return this;
        }

        @Override
        public Builder setPageSlots(@NotNull List<Integer> pageSlots) {
            this.pageSlots = pageSlots;
            return this;
        }

        @Override
        public SimplePagedGui build() {
            return new SimplePagedGui(title, size, type, onClose, onOpen, onDrag, onTick, onClickGlobal, onPageChange, playerManipulation, pageSlots);
        }
    }

    /** Paged-GUI listener variant; inherits all handling from {@link AbstractGui.GuiListener}. */
    public static class GuiListener extends AbstractGui.GuiListener implements IPagedGuiListener {
    }
}
