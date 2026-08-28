package at.woodexplosive.woodlib.gui.gui;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.event.*;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import at.woodexplosive.woodlib.api.gui.gui.builder.IGuiBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * A basic single-page {@link IGui}. Create one through {@link #builder(Component, int, IGui)} (or the
 * {@link InventoryType} variant) or extending a class, configure its callbacks, {@link Builder#build() build} it, then
 * {@link #open(org.bukkit.entity.Player) open} it for a player.
 *
 * <pre>{@code
 * SimpleGui gui = SimpleGui.builder(Component.text("Menu"), 27)
 *         .setPlayerManipulation(false)
 *         .build();
 * gui.setSlot(13, GuiElement.builder(Material.DIAMOND).build...);
 * gui.open(player);
 * }</pre>
 */
public class SimpleGui extends AbstractGui<SimpleGui> {

    /**
     * @param title              the inventory title
     * @param size               the inventory size (multiple of 9); ignored if {@code type} is non-null
     * @param type               the inventory type, or {@code null} to create a plain chest inventory of {@code size}
     * @param onClose            the close callback
     * @param onOpen             the open callback
     * @param onInteract         the interact callback
     * @param onDrag             the drag callback
     * @param onTick             the per-tick callback
     * @param onClickGlobal      the global click callback
     * @param playerManipulation {@code true} to allow the player to move items in the inventory
     * @param parent             the parent Gui can be null if there's none
     */
    private SimpleGui(@NotNull Component title, int size, @Nullable InventoryType type, @NotNull Callback<GuiCloseEvent> onClose, @NotNull Callback<GuiOpenEvent> onOpen, @NotNull Callback<GuiInteractEvent> onInteract,
                      @NotNull Callback<GuiDragEvent> onDrag,
                      @NotNull Callback<GuiTickEvent> onTick, IGuiElement.@NotNull ClickCallback onClickGlobal, boolean playerManipulation, IGui<?> parent) {
        super(title, size, type, onClose, onOpen, onInteract, onDrag, onTick, onClickGlobal, playerManipulation, parent);
    }

    /**
     * Template-method constructor for subclasses: builds the inventory from the overridable hooks
     * inherited from {@link AbstractGui} (e.g. {@link #title()}, {@link #size()}), then runs
     * {@link #init()}.
     */
    protected SimpleGui() {
        super();

        this.init();
    }

    /**
     * Starts a builder for a Simple GUI of the given title and size.
     * @param title the inventory title
     * @param size the inventory size (multiple of 9)
     * @param parent the parent Gui can be null if there's none
     * @return a new {@link Builder}
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static Builder builder(Component title, int size, @Nullable IGui<?> parent) {
        return new Builder(title, size, parent);
    }

    /**
     * Starts a builder for a Simple GUI of the given title and {@link InventoryType}.
     * @param title the inventory title
     * @param type the inventory type (its default size is used)
     * @param parent the parent Gui can be null if there's none
     * @return a new {@link Builder}
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static Builder builder(Component title, @NotNull InventoryType type, @Nullable IGui<?> parent) {
        return new Builder(title, type, parent);
    }

    /**
     * Fluent builder for {@link SimpleGui}.
     */
    public static class Builder implements IGuiBuilder<Builder, SimpleGui> {
        private final int size;
        private final Component title;
        private final InventoryType type;
        private final @Nullable IGui<?> parent;

        private boolean playerManipulation = false;
        private Callback<GuiCloseEvent> onClose = IGui.emptyCallback();
        private Callback<GuiOpenEvent> onOpen = IGui.emptyCallback();
        private Callback<GuiInteractEvent> onInteract = IGui.emptyCallback();
        private Callback<GuiDragEvent> onDrag = IGui.emptyCallback();
        private Callback<GuiTickEvent> onTick = IGui.emptyCallback();
        private IGuiElement.ClickCallback onClickGlobal = IGuiElement.EMPTY_CALLBACK;

        /**
         * @param title the inventory title
         * @param size the inventory size (multiple of 9)
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
        public Builder setPlayerManipulation(boolean playerManipulation) {
            this.playerManipulation = playerManipulation;
            return this;
        }

        @Override
        public @NonNull SimpleGui build() {
            return new SimpleGui(this.title, this.size, this.type, this.onClose, this.onOpen, this.onInteract, this.onDrag, this.onTick, this.onClickGlobal, this.playerManipulation, parent);
        }
    }
}
