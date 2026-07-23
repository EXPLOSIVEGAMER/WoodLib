package at.woodexplosive.woodlib.gui.gui;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.event.GuiClickEvent;
import at.woodexplosive.woodlib.api.gui.event.GuiTickEvent;
import at.woodexplosive.woodlib.api.gui.gui.IGui;
import at.woodexplosive.woodlib.api.gui.gui.Listener.IGuiListener;
import at.woodexplosive.woodlib.gui.element.GuiElement;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Base implementation of {@link IGui} shared by all concrete GUIs. Owns the backing inventory (whose
 * holder is the GUI itself), the slot elements and their callbacks, the configured event callbacks,
 * and the per-tick update task.
 *
 * <p>Event delivery goes through the nested {@link GuiListener}, which WoodLib registers once on
 * initialization; it dispatches Bukkit inventory events to the GUI that holds the inventory.</p>
 *
 * @param <T> the concrete GUI type, for fluent self-returning methods (CRTP)
 */
public abstract class AbstractGui<T extends IGui<T>> implements IGui<T> {
    /** The inventory title. */
    protected final Component title;
    /** The backing Bukkit inventory, whose holder is this GUI. */
    protected final Inventory inventory;
    /** Elements currently placed, keyed by slot index. */
    protected final Map<Integer, IGuiElement> elements = new HashMap<>();
    /**
     * Elements that carry a callback, keyed by their stable {@link IGuiElement#getId() id}, so a
     * moved item still resolves to its element (see {@link #resolveElement(int, ItemStack)}).
     */
    protected final Map<UUID, IGuiElement> elementsById = new HashMap<>();
    /** Standalone click callbacks set via {@link #setSlotCallback(int, IGuiElement.ClickCallback)}, keyed by slot index. */
    protected final Map<Integer, IGuiElement.ClickCallback> slotCallbacks = new HashMap<>();
    /** Callback run when the inventory is closed. */
    protected final Callback<InventoryCloseEvent> onClose;
    /** Callback run when the inventory is opened. */
    protected final Callback<InventoryOpenEvent> onOpen;
    /** Callback run when items are dragged across the inventory. */
    protected final Callback<InventoryDragEvent> onDrag;
    /** Callback run every server tick while the GUI is open. */
    protected final Callback<GuiTickEvent> onTick;
    /** Global click callback run for every click in the GUI, before per-slot callbacks. */
    protected final IGuiElement.ClickCallback onClickGlobal;
    /** Whether the player may freely move items in the inventory. */
    protected final boolean playerManipulation;

    /** The player this GUI was last opened for. */
    private Player player;
    /** The currently scheduled per-tick update task, or {@code null} while not ticking. */
    private BukkitTask tickTask;

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
    protected AbstractGui(@NotNull Component title, int size, @Nullable InventoryType type, @NotNull Callback<InventoryCloseEvent> onClose, @NotNull Callback<InventoryOpenEvent> onOpen, @NotNull Callback<InventoryDragEvent> onDrag,
                          @NotNull Callback<GuiTickEvent> onTick, IGuiElement.@NotNull ClickCallback onClickGlobal, boolean playerManipulation) {

        this.inventory = type != null
                ? Bukkit.createInventory(self(), type, title)
                : Bukkit.createInventory(self(), size, title);

        this.title = title;
        this.onClose = onClose;
        this.onOpen = onOpen;
        this.onDrag = onDrag;
        this.onTick = onTick;
        this.onClickGlobal = onClickGlobal;
        this.playerManipulation = playerManipulation;
    }

    /**
     * Casts this GUI to its concrete type, for fluent self-returning methods.
     * @return this GUI as {@code T}
     */
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    @Override
    public T setSlot(int slot, @NotNull IGuiElement element) {
        IGuiElement previous = this.elements.get(slot);
        if (previous != null && previous.hasCallback()) this.elementsById.remove(previous.getId());

        this.inventory.setItem(slot, element.getItemStackForDisplay());
        this.elements.put(slot, element);
        if (element.hasCallback()) this.elementsById.put(element.getId(), element);
        return self();
    }

    /**
     * Resolves the {@link IGuiElement} that should handle a click on the given raw slot: prefers the
     * element identified by the id stamped on {@code stack} (see {@link IGuiElement#getItemStackForDisplay()}),
     * so a moved item keeps its callback even after leaving the slot it was originally placed in, and
     * falls back to the slot's tracked element (or an empty element) otherwise.
     *
     * @param rawSlot the raw slot that was clicked
     * @param stack   the item stack currently occupying that slot
     * @return the resolved {@link IGuiElement}
     */
    private IGuiElement resolveElement(int rawSlot, @Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            String rawId = meta.getPersistentDataContainer().get(IGuiElement.idKey(), PersistentDataType.STRING);
            if (rawId != null) {
                IGuiElement byId = this.elementsById.get(UUID.fromString(rawId));
                if (byId != null) return byId;
            }
        }
        return this.elements.getOrDefault(rawSlot, GuiElement.empty());
    }

    @Override
    public T setSlots(Map<Integer, @NotNull IGuiElement> elements) {
        for (Map.Entry<Integer, IGuiElement> entry : elements.entrySet()) {
            this.setSlot(entry.getKey(), entry.getValue());
        }

        return self();
    }

    @Override
    public T setSlotCallback(int index, @NotNull IGuiElement.ClickCallback callback) {
        this.slotCallbacks.put(index, callback);
        return self();
    }

    @Override
    public T setSlotCallbacks(Map<Integer, IGuiElement.@NotNull ClickCallback> callbacks) {
        return self();
    }

    @Override
    public T addSlot(@NotNull IGuiElement element) {
        int slot = this.inventory.firstEmpty();
        if (slot != -1) {
            this.setSlot(slot, element);
        } else {
            WoodLib.logger().error("There are no more slots empty in {}!", this);
        }
        return self();
    }

    @Override
    public T addSlots(@NotNull List<IGuiElement> elements) {
        for (IGuiElement element : elements) {
            this.addSlot(element);
        }
        return self();
    }

    @Override
    public @NonNull Component getTitle() {
        return this.title;
    }

    @Contract(pure = true)
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public int close() {
        return this.inventory.close();
    }

    @Override
    public @Nullable Player getPlayer() {
        return this.player;
    }

    @Override
    public @Nullable InventoryView open(@NonNull Player player) {
        this.player = player;
        InventoryView view = player.openInventory(this.inventory);
        startTicking();
        return view;
    }

    @Override
    public void startTicking() {
        if (this.tickTask != null) this.tickTask.cancel();
        this.tickTask = Bukkit.getScheduler().runTaskTimer(
                WoodLib.plugin(),
                this::tick,
                0L,
                1L
        );
    }

    @Override
    public void stopTicking() {
        if (this.tickTask == null) return;
        this.tickTask.cancel();
        this.tickTask = null;
    }

    protected void tick() {
        GuiTickEvent event = new GuiTickEvent(self());
        event.callEvent();
        this.onTick.run(event);
    }

    /**
     * The single Bukkit listener that routes inventory events to the {@link AbstractGui} holding the
     * affected inventory. Registered once by WoodLib on initialization.
     */
    public static class GuiListener implements IGuiListener {

        @EventHandler
        @Override
        public void onInventoryClose(@NonNull InventoryCloseEvent event) {
            if (!(event.getInventory().getHolder() instanceof AbstractGui<?> gui)) return;
            gui.stopTicking();
            gui.onClose.run(event);
        }

        @EventHandler(ignoreCancelled = true)
        @Override
        public void onInventoryOpen(@NonNull InventoryOpenEvent event) {
            if (event.getInventory().getHolder() instanceof AbstractGui<?> gui) {
                boolean cancel = gui.onOpen.run(event);
                event.setCancelled(cancel);
            }
        }

        @EventHandler(ignoreCancelled = true)
        @Override
        public void onInventoryClick(@NonNull InventoryClickEvent event) {
            if (event.getInventory().getHolder() instanceof AbstractGui<?> gui) {
                event.setCancelled(handleClick(gui, event));
            }
        }

        // IGuiElement isn't parameterized by GUI type (an element can be placed in any AbstractGui<T>),
        // so its ClickCallback is necessarily raw here.
        private static boolean handleClick(AbstractGui<?> gui, InventoryClickEvent event) {
            boolean cancel = !gui.playerManipulation;

            if (gui.onClickGlobal.click(new GuiClickEvent(gui, null, event))) cancel = true;

            IGuiElement element = gui.resolveElement(event.getRawSlot(), event.getCurrentItem());
            if (element != null
                    && element.getCallback().click(new GuiClickEvent(gui, element, event))) cancel = true;

            if (gui.slotCallbacks.getOrDefault(event.getRawSlot(), IGuiElement.EMPTY_CALLBACK).click(new GuiClickEvent(gui, element, event)))
                cancel = true;

            if (!(new GuiClickEvent(gui, element, event).callEvent())) cancel = true;

            return cancel;
        }

        @EventHandler(ignoreCancelled = true)
        @Override
        public void onInventoryDrag(@NonNull InventoryDragEvent event) {
            if (event.getInventory().getHolder() instanceof AbstractGui<?> gui) {
                boolean cancel = gui.onDrag.run(event);
                event.setCancelled(cancel);
            }
        }

    }
}
