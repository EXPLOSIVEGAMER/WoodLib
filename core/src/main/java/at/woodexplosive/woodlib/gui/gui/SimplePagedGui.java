package at.woodexplosive.woodlib.gui.gui;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.event.*;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import at.woodexplosive.woodlib.api.gui.gui.IPagedGui;
import at.woodexplosive.woodlib.api.gui.gui.builder.IPagedGuiBuilder;
import at.woodexplosive.woodlib.gui.element.GuiElementBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * A multipage {@link IPagedGui}: page elements are spread across the configured page slots and
 * navigated with page controls. Create one through {@link #builder(Component, int, IGui)} or extending a class, add elements via
 * {@link #addPageElement(at.woodexplosive.woodlib.api.gui.element.IGuiElement)}, optionally place
 * next/previous controls, then open it. Opening (re)populates the current page automatically.
 */
public class SimplePagedGui extends AbstractGui<SimplePagedGui> implements IPagedGui<SimplePagedGui> {
    /**
     * The slot indices that page elements are laid out into.
     */
    private final List<Integer> pageSlots;

    /**
     * Callback run when the page changes; returning {@code true} cancels the change.
     */
    protected final Callback<GuiPageChangeEvent> onPageChange;

    /**
     * Backing list of all page elements (across every page).
     */
    private LinkedList<IGuiElement> pageElements = new LinkedList<>();

    /**
     * The current (0-based) page index.
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
     * @param playerManipulation {@code true} to allow the player to move items in the inventory
     * @param pageSlots          the slot indices that page elements are laid out into
     * @param parent             the parent Gui can be null if there's none
     */
    private SimplePagedGui(@NotNull Component title, int size, @Nullable InventoryType type, @NotNull Callback<GuiCloseEvent> onClose,
                           @NotNull Callback<GuiOpenEvent> onOpen, @NotNull Callback<GuiInteractEvent> onInteract, @NotNull Callback<GuiDragEvent> onDrag,
                           @NotNull Callback<GuiTickEvent> onTick, IGuiElement.@NotNull ClickCallback onClickGlobal,
                           @NotNull Callback<GuiPageChangeEvent> onPageChange,
                           boolean playerManipulation, @NotNull List<Integer> pageSlots, @Nullable IGui<?> parent
    ) {

        super(title, size, type, onClose, onOpen, onInteract, onDrag, onTick, onClickGlobal, playerManipulation, parent);
        this.onPageChange = onPageChange;
        this.pageSlots = pageSlots;
    }

    /**
     * Template-method constructor for subclasses: builds the inventory from the {@link AbstractGui}
     * hooks plus {@link #pageSlots()} and {@link #onPageChange(GuiPageChangeEvent)}, then runs
     * {@link #init()}.
     */
    protected SimplePagedGui() {
        super();
        this.pageSlots = pageSlots();
        this.onPageChange = this::onPageChange;

        this.init();
    }

    /**
     * The slot indices that page elements are laid out into. Overridden by no-arg-constructor
     * subclasses; defaults to no page slots.
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
     * Starts a builder for a paged GUI of the given title and size.
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
     * Starts a builder for a paged GUI of the given title and {@link InventoryType}.
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

    @Override
    public SimplePagedGui setPage(int page) {
        GuiPageChangeEvent event = new GuiPageChangeEvent(this, getMaxPage(), this.page, page);
        if (event.callEvent() && !this.onPageChange.run(event)) {
            this.page = page;
            this.redraw();
        }
        return this;
    }

    @Override
    public @NonNull LinkedList<IGuiElement> getPageElements() {
        return this.pageElements;
    }

    @Override
    public SimplePagedGui addPageElement(@NonNull IGuiElement element) {
        this.pageElements.add(element);
        return this;
    }

    @Override
    public SimplePagedGui addPageElements(@NonNull Collection<? extends IGuiElement> elements) {
        this.pageElements.addAll(elements);
        return this;
    }

    @Override
    public SimplePagedGui removePageElement(@NonNull IGuiElement element) {
        this.pageElements.remove(element);
        return this;
    }

    @Override
    public SimplePagedGui removePageElements(@NonNull Collection<? extends IGuiElement> elements) {
        this.pageElements.removeAll(elements);
        return this;
    }

    @Override
    public SimplePagedGui setPageElement(@NonNull LinkedList<? extends IGuiElement> elements) {
        this.pageElements = new LinkedList<>(elements);
        return this;
    }

    @Override
    public int getPage() {
        return this.page;
    }

    @Override
    public @NonNull List<Integer> getPageSlots() {
        return this.pageSlots;
    }

    @Override
    public SimplePagedGui setNextPageElement(int slot, @NonNull IGuiElement element) {
        if (!element.hasCallback()) element = GuiElementBuilder.of(element).setCallback(event -> {
            this.nextPage();
            return true;
        }).buildElement();

        this.setSlot(slot, element);
        return this;
    }

    @Override
    public SimplePagedGui setPreviousPageElement(int slot, @NonNull IGuiElement element) {
        if (!element.hasCallback()) element = GuiElementBuilder.of(element).setCallback(event -> {
            this.previousPage();
            return true;
        }).buildElement();

        this.setSlot(slot, element);
        return this;
    }

    @Override
    public SimplePagedGui addSlot(@NotNull IGuiElement element) {
        int slot = this.firstTrueEmpty();
        if (slot != -1) {
            this.setSlot(slot, element);
        } else {
            WoodLib.logger().error("There are no more slots empty in {}!", this);
        }
        return this;
    }

    @Override
    public @Nullable InventoryView open(@NonNull Player player, int page) {
        this.page = page;
        this.populatePage();
        return super.open(player);
    }

    @Override
    public @Nullable InventoryView open(@NonNull Player player) {
        return this.open(player, 0);
    }

    // Builder

    /**
     * Fluent builder for {@link SimplePagedGui}.
     */
    public static class Builder implements IPagedGuiBuilder<Builder, SimplePagedGui> {
        private final int size;
        private final Component title;
        private final InventoryType type;
        private final @Nullable IGui<?> parent;

        private List<Integer> pageSlots = new ArrayList<>();
        private boolean playerManipulation;
        private Callback<GuiCloseEvent> onClose = IGui.emptyCallback();
        private Callback<GuiOpenEvent> onOpen = IGui.emptyCallback();
        private Callback<GuiInteractEvent> onInteract = IGui.emptyCallback();
        private Callback<GuiDragEvent> onDrag = IGui.emptyCallback();
        private Callback<GuiTickEvent> onTick = IGui.emptyCallback();
        private Callback<GuiPageChangeEvent> onPageChange = IGui.emptyCallback();
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
        public Builder setOnTick(@NotNull IGui.Callback<GuiTickEvent> onTick) {
            this.onTick = onTick;
            return this;
        }

        @Override
        public Builder setOnClickGlobal(@NotNull IGuiElement.ClickCallback onClickGlobal) {
            this.onClickGlobal = onClickGlobal;
            return this;
        }


        @Override
        public Builder setOnPageChange(@NotNull Callback<GuiPageChangeEvent> onPageChange) {
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
        public @NonNull SimplePagedGui build() {
            return new SimplePagedGui(title, size, type, onClose, onOpen, onInteract, onDrag, onTick, onClickGlobal, onPageChange, playerManipulation, pageSlots, parent);
        }
    }
}
