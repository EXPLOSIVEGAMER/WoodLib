package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.gui.IGui;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all Bukkit events fired by a WoodLib {@link IGui}. Carries the GUI the event
 * originated from.
 *
 */
public abstract class GuiEvent extends Event implements IGuiEvent {
    /** The GUI this event was fired for. */
    protected final IGui<?> gui;

    /**
     * @param gui the GUI this event is fired for
     */
    public GuiEvent(@NotNull IGui<?> gui) {
        this.gui = gui;
    }

    /**
     * Returns the GUI this event was fired for.
     * @return the source GUI
     */
    @Override
    public @NotNull IGui<?> getGui() {
        return gui;
    }
}
