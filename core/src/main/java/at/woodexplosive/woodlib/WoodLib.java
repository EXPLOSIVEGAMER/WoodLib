package at.woodexplosive.woodlib;

import at.woodexplosive.woodlib.gui.gui.AbstractGui;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central entry point and runtime holder of the WoodLib library.
 *
 * <p>WoodLib is bound to a single host {@link JavaPlugin}. Whichever plugin owns the library at
 * runtime must call {@link #init(JavaPlugin)} once during its {@code onEnable} and
 * {@link #disable()} during its {@code onDisable}:</p>
 * <ul>
 *     <li><b>Standalone:</b> the bundled {@code WoodLibPlugin} calls these itself; consumers only
 *     declare {@code depend: [WoodLib]} and use the API.</li>
 *     <li><b>Shaded:</b> the consuming plugin shades the library and calls {@link #init(JavaPlugin)}
 *     with itself as the host.</li>
 * </ul>
 *
 * <p>After initialization {@link #plugin()} and {@link #logger()} expose the host plugin and a
 * library logger scoped to it.</p>
 */
public final class WoodLib {
    /** Stable library identifier, also used as the logger name prefix. */
    public static final String LIB_ID = "woodlib";
    /** Library logger, scoped to the host plugin. {@code null} until {@link #init(JavaPlugin)} ran. */
    public static Logger logger;
    private static JavaPlugin plugin;

    private WoodLib() {}

    /**
     * Initializes the library and binds it to the given host plugin. Registers the GUI listener and
     * starts the {@link Scheduler}. Call once from the host's
     * {@code onEnable}.
     *
     * @param host the plugin that owns the library at runtime
     * @throws IllegalStateException if the library is already initialized
     */
    public static void init(JavaPlugin host) {
        if (plugin != null) throw new IllegalStateException("WoodLib is already initialized");
        plugin = host;
        logger = LoggerFactory.getLogger(String.format("%s:%s", LIB_ID, host.getName()));

        Scheduler.start();
        rListeners();
    }

    /**
     * Tears the library down and stops the {@link Scheduler}.
     * Call from the host's {@code onDisable}. Does nothing if the library was never initialized.
     */
    public static void disable() {
        if (plugin == null) return;

        Scheduler.stop();
        plugin = null;
        logger = null;
    }

    /**
     * Returns the host plugin the library is bound to.
     *
     * @return the owning {@link JavaPlugin}
     * @throws IllegalStateException if {@link #init(JavaPlugin)} was not called
     */
    public static JavaPlugin plugin() {
        if (plugin == null) throw new IllegalStateException("WoodLib.init(plugin) was not called");
        return plugin;
    }

    /**
     * Returns the library logger, scoped to the host plugin.
     *
     * @return the {@link Logger}
     * @throws IllegalStateException if {@link #init(JavaPlugin)} was not called
     */
    public static Logger logger() {
        if (logger == null) throw new IllegalStateException("WoodLib.init(plugin) was not called");
        return logger;
    }

    private static void rListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new AbstractGui.GuiListener(), plugin());
    }
}
