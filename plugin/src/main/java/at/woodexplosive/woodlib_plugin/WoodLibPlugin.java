package at.woodexplosive.woodlib_plugin;

import at.woodexplosive.woodlib.WoodLib;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Standalone plugin entry point for the WoodLib library jar: initializes and tears down
 * {@link WoodLib} with itself as the host plugin, so other plugins can just {@code depend: [WoodLib]}
 * and use the API without shading the library themselves.
 */
public final class WoodLibPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        WoodLib.init(this);
    }

    @Override
    public void onDisable() {
        WoodLib.disable();
    }
}
