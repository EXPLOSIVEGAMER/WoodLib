package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.element.ITab;
import at.woodexplosive.woodlib.api.gui.gui.ITabbedGui;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when an {@link ITabbedGui} is about to change its active tab. Cancelling prevents the change.
 *
 * @param <T> the concrete tabbed-GUI type
 */
public class GuiTabChangeEvent<T extends ITabbedGui<T>> extends GuiEvent<T> implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final ITab oldTab;
    private final ITab newTab;

    private boolean cancelled = false;

    /**
     * @param gui the tabbed GUI
     * @param oldTab the tab active before the change (may be {@code null})
     * @param newTab the tab being switched to
     */
    public GuiTabChangeEvent(@NotNull T gui, @Nullable ITab oldTab, @NotNull ITab newTab) {
        super(gui);
        this.oldTab = oldTab;
        this.newTab = newTab;
    }

    /**
     * @return the tab active before the change, or {@code null} if none was active
     */
    public @Nullable ITab getOldTab() {
        return this.oldTab;
    }

    /**
     * @return the tab being switched to
     */
    public @NotNull ITab getNewTab() {
        return this.newTab;
    }

    /**
     * @return {@code true} if the new tab differs from the old one
     */
    public boolean hasChanged() {
        return this.oldTab != this.newTab;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Bukkit handler list accessor.
     * @return the {@link HandlerList}
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
