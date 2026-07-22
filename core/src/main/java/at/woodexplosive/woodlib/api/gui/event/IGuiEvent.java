package at.woodexplosive.woodlib.api.gui.event;

import at.woodexplosive.woodlib.api.gui.gui.IGui;
import org.jetbrains.annotations.NotNull;

/**
 * Common contract for events fired by a WoodLib {@link IGui}, exposing the GUI the event originated
 * from. Implemented by {@link GuiEvent} (custom Bukkit events) and separately by {@link GuiClickEvent}
 * (which extends the native {@link org.bukkit.event.inventory.InventoryClickEvent} instead).
 */
public interface IGuiEvent {

    /**
     * Returns the GUI this event was fired for.
     * @return the source GUI
     */
    @NotNull IGui<?> getGui();

    /**
     * Casts {@link #getGui()} to a specific GUI type, for handlers that know the concrete GUI they're
     * dealing with.
     * @param clazz the expected concrete GUI class
     * @param <G> the concrete GUI type
     * @return the source GUI, cast to {@code G}
     * @throws ClassCastException if the GUI is not an instance of {@code clazz}
     */
    default <G extends IGui<G>> G castGui(@NotNull Class<G> clazz) {
        return clazz.cast(this.getGui());
    }
}
