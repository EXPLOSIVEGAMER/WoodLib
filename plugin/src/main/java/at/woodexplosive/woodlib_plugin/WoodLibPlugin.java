package at.woodexplosive.woodlib_plugin;

import at.woodexplosive.woodlib.WoodLib;
import org.bukkit.plugin.java.JavaPlugin;

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
