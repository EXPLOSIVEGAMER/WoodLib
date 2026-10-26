package at.woodexplosive.woodlib;

import at.woodexplosive.woodlib.api.block.CustomBlockRegistry;
import at.woodexplosive.woodlib.api.uninode.UniNodespace;
import at.woodexplosive.woodlib.impl.Scheduler;
import at.woodexplosive.woodlib.impl.block.CustomBlockDigging;
import at.woodexplosive.woodlib.impl.block.CustomBlockEntityListener;
import at.woodexplosive.woodlib.impl.block.CustomBlockEntityManager;
import at.woodexplosive.woodlib.impl.block.CustomBlockListener;
import at.woodexplosive.woodlib.impl.block.CustomBlockPlacementPreview;
import at.woodexplosive.woodlib.impl.gui.gui.AbstractGui;
import at.woodexplosive.woodlib.impl.command.DebugCommand;
import at.woodexplosive.woodlib.impl.uninode.NodeSpaceListener;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import com.github.retrooper.packetevents.PacketEvents;
import com.jeff_media.customblockdata.CustomBlockData;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
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
    private static Logger logger;
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
    @SuppressWarnings("UnstableApiUsage")
    public static void init(JavaPlugin host) {
        if (plugin != null) throw new IllegalStateException("WoodLib is already initialized");
        plugin = host;
        logger = LoggerFactory.getLogger(String.format("%s:%s", LIB_ID, host.getName()));

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(host));

        PacketEvents.getAPI().getSettings().checkForUpdates(false);
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().init();

        Scheduler.start();
        rListeners();
        rCommands();
    }

    /**
     * Tears the library down, stops the {@link Scheduler} and terminates PacketEvents.
     * Call from the host's {@code onDisable}. Does nothing if the library was never initialized.
     */
    public static void disable() {
        if (plugin == null) return;

        Scheduler.stop();
        CustomBlockEntityManager.unloadAll();
        CustomBlockRegistry.clear();
        PacketEvents.getAPI().terminate();
        UniNodespace.clearNodes();
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

    /** Registers the library's commands under the host plugin; works without a {@code plugin.yml} entry. */
    @SuppressWarnings("UnstableApiUsage")
    private static void rCommands() {
        plugin().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register("woodlib", "WoodLib debug command", new DebugCommand()));
    }

    private static void rListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new NodeSpaceListener(), plugin());

        pm.registerEvents(new AbstractGui.GuiListener(), plugin());

        pm.registerEvents(new CustomBlockListener(), plugin());
        pm.registerEvents(new CustomBlockDigging(), plugin());
        CustomBlockDigging.registerPacketListener();
        CustomBlockPlacementPreview.start();
        pm.registerEvents(new CustomBlockEntityListener(), plugin());
        CustomBlockEntityManager.start();
        CustomBlockData.registerListener(plugin());
    }
}
