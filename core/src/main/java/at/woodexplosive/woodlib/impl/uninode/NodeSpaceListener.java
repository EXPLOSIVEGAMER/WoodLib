package at.woodexplosive.woodlib.impl.uninode;

import at.woodexplosive.woodlib.api.uninode.UniNodespace;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;

public class NodeSpaceListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onServerTickStart(ServerTickStartEvent event) {
        UniNodespace.updateNodespace();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        UniNodespace.removeWorld(event.getWorld());
    }

}
