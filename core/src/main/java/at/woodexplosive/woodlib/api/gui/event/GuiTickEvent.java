package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.gui.IGui;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired once per server tick for every open {@link IGui}, driving per-tick GUI updates.
 *
 * @param <T> the concrete GUI type
 */
public class GuiTickEvent<T extends IGui<T>> extends GuiEvent<T> {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * @param gui the ticking GUI
     */
    public GuiTickEvent(@NotNull T gui) {
        super(gui);
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
