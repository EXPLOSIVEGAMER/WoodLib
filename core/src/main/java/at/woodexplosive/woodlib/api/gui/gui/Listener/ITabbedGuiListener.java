package at.woodexplosive.woodlib.api.gui.gui.Listener;

import at.woodexplosive.woodlib.api.gui.event.GuiTabChangeEvent;
import at.woodexplosive.woodlib.api.gui.gui.ITabbedGui;

/**
 * {@link IGuiListener} specialization for tabbed GUIs, adding a tab-change hook.
 *
 * @param <T> the concrete tabbed-GUI type
 */
public interface ITabbedGuiListener<T extends ITabbedGui<T>> extends IGuiListener {

    /**
     * Handles the active tab changing.
     * @param event the {@link GuiTabChangeEvent}
     */
    void onTabChange(GuiTabChangeEvent<T> event);

}
