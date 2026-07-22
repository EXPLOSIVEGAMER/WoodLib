package at.woodexplosive.woodlib.gui.gui;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
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
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    protected final Component title;
    protected final Inventory inventory;
    protected final Map<Integer, IGuiElement> elements = new HashMap<>();
    protected final Map<Integer, IGuiElement.ClickCallback> slotCallbacks = new HashMap<>();
    protected final Callback<InventoryCloseEvent> onClose;
    protected final Callback<InventoryOpenEvent> onOpen;
    protected final Callback<InventoryDragEvent> onDrag;
    protected final Callback<GuiTickEvent<T>> onTick;
    protected final IGuiElement.ClickCallback onClickGlobal;
    protected final boolean playerManipulation;

    private Player player;
    private BukkitTask tickTask;

    protected AbstractGui(Component title, int size, InventoryType type, Callback<InventoryCloseEvent> onClose, Callback<InventoryOpenEvent> onOpen, Callback<InventoryDragEvent> onDrag,
                          Callback<GuiTickEvent<T>> onTick, IGuiElement.ClickCallback onClickGlobal, boolean playerManipulation) {

        this.inventory = type != null
                ? Bukkit.createInventory(this, type, title)
                : Bukkit.createInventory(this, size, title);

        this.title = title;
        this.onClose = onClose;
        this.onOpen = onOpen;
        this.onDrag = onDrag;
        this.onTick = onTick;
        this.onClickGlobal = onClickGlobal;
        this.playerManipulation = playerManipulation;
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    @Override
    public T setSlot(int slot, @NotNull IGuiElement element) {
        this.inventory.setItem(slot, element.getItemStackForDisplay());
        this.elements.put(slot, element);
        return self();
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
    public Player getPlayer() {
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
        GuiTickEvent<T> event = new GuiTickEvent<>(self());
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
                boolean cancel = !gui.playerManipulation;

                if (gui.onClickGlobal.click(event, gui, null, event.getClick(), event.getAction())) cancel = true;

                if (event.getRawSlot() >= 0 && event.getRawSlot() < event.getInventory().getSize()) {
                    IGuiElement element = gui.elements.getOrDefault(event.getRawSlot(), GuiElement.empty());
                    if (element.getCallback().click(event, gui, element, event.getClick(), event.getAction())
                            || gui.slotCallbacks.getOrDefault(event.getRawSlot(), IGuiElement.EMPTY_CALLBACK).click(event, gui, null, event.getClick(), event.getAction())) {
                        cancel = true;
                    }
                }

                event.setCancelled(cancel);
            }
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
