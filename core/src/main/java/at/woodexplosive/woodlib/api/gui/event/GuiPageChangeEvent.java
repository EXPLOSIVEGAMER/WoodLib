package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.gui.IGui;
import at.woodexplosive.woodlib.api.gui.gui.IPagedGui;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when an {@link IPagedGui} is about to change its page. Cancelling the event prevents the
 * page change.
 */
public class GuiPageChangeEvent extends GuiEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final int maxPage;
    private final int oldPage;
    private final int newPage;

    private boolean cancelled = false;

    /**
     * @param gui the paged GUI
     * @param maxPage the highest valid page index
     * @param oldPage the page before the change
     * @param newPage the page being switched to
     */
    public GuiPageChangeEvent(@NotNull IGui<?> gui, int maxPage, int oldPage, int newPage) {
        super(gui);
        this.maxPage = maxPage;
        this.oldPage = oldPage;
        this.newPage = newPage;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * @return the highest valid page index
     */
    public int getMaxPage() {
        return maxPage;
    }

    /**
     * @return the page being switched to
     */
    public int getNewPage() {
        return newPage;
    }

    /**
     * @return the page before the change
     */
    public int getOldPage() {
        return oldPage;
    }

    /**
     * @return {@code true} if the new page differs from the old one
     */
    public boolean hasChanged() {
        return this.newPage != this.oldPage;
    }

    /**
     * @return {@code true} if the new page is the first page ({@code 0})
     */
    public boolean isStartPage() {
        return this.newPage == 0;
    }

    /**
     * @return {@code true} if the new page is the last page
     */
    public boolean isMaxPage() {
        return this.newPage == this.maxPage;
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
