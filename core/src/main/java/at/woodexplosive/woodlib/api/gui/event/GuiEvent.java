package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.gui.IGui;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all Bukkit events fired by a WoodLib {@link IGui}. Carries the GUI the event
 * originated from.
 *
 * @param <T> the concrete GUI type
 */
public abstract class GuiEvent<T extends IGui<T>> extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    /** The GUI this event was fired for. */
    protected final T gui;

    /**
     * @param gui the GUI this event is fired for
     */
    public GuiEvent(@NotNull T gui) {
        this.gui = gui;
    }

    /**
     * Returns the GUI this event was fired for.
     * @return the source GUI
     */
    public @NotNull T getGui() {
        return gui;
    }
}
