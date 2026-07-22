package at.woodexplosive.woodlib.gui.gui;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.event.GuiTickEvent;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import at.woodexplosive.woodlib.api.gui.gui.builder.IGuiBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * A basic single-page {@link IGui}. Create one through {@link #builder(Component, int)} (or the
 * {@link InventoryType} variant), configure its callbacks, {@link Builder#build() build} it, then
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
     * @param title the inventory title
     * @param size the inventory size (multiple of 9); ignored if {@code type} is non-null
     * @param type the inventory type, or {@code null} to create a plain chest inventory of {@code size}
     * @param onClose the close callback
     * @param onOpen the open callback
     * @param onDrag the drag callback
     * @param onTick the per-tick callback
     * @param onClickGlobal the global click callback
     * @param playerManipulation {@code true} to allow the player to move items in the inventory
     */
    protected SimpleGui(@NotNull Component title, int size, @Nullable InventoryType type, @NotNull Callback<InventoryCloseEvent> onClose, @NotNull Callback<InventoryOpenEvent> onOpen, @NotNull Callback<InventoryDragEvent> onDrag,
                        @NotNull Callback<GuiTickEvent> onTick, IGuiElement.@NotNull ClickCallback onClickGlobal, boolean playerManipulation) {
        super(title, size, type, onClose, onOpen, onDrag, onTick, onClickGlobal, playerManipulation);
    }

    /**
     * Starts a builder for a chest-like GUI of the given title and size.
     * @param title the inventory title
     * @param size the inventory size (multiple of 9)
     * @return a new {@link Builder}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static Builder builder(Component title, int size) {
        return new Builder(title, size);
    }

    /**
     * Starts a builder for a GUI of the given title and {@link InventoryType}.
     * @param title the inventory title
     * @param type the inventory type (its default size is used)
     * @return a new {@link Builder}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static Builder builder(Component title, @NotNull InventoryType type) {
        return new Builder(title, type);
    }

    /** Fluent builder for {@link SimpleGui}. */
    public static class Builder implements IGuiBuilder<Builder, SimpleGui> {
        private final int size;
        private final Component title;
        private final InventoryType type;

        private boolean playerManipulation = false;
        private Callback<InventoryCloseEvent> onClose = IGui.emptyCallback();
        private Callback<InventoryOpenEvent> onOpen = IGui.emptyCallback();
        private Callback<InventoryDragEvent> onDrag = IGui.emptyCallback();
        private Callback<GuiTickEvent> onTick = IGui.emptyCallback();
        private IGuiElement.ClickCallback onClickGlobal = IGuiElement.EMPTY_CALLBACK;

        /**
         * @param title the inventory title
         * @param size the inventory size (multiple of 9)
         */
        protected Builder(Component title, int size) {
            this.title = title;
            this.size = size;
            this.type = null;
        }

        /**
         * @param title the inventory title
         * @param type the inventory type (its default size is used)
         */
        protected Builder(Component title, InventoryType type) {
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
        public Builder setOnOpen(@NotNull IGui.Callback<InventoryOpenEvent> onOpen) {
            this.onOpen = onOpen;
            return this;
        }

        @Override
        public Builder setOnDrag(@NotNull IGui.Callback<InventoryDragEvent> onDrag) {
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
            return new SimpleGui(this.title, this.size, this.type, this.onClose, this.onOpen, this.onDrag, this.onTick, this.onClickGlobal, this.playerManipulation);
        }
    }
}
