package at.woodexplosive.woodlib.impl.block;

import at.woodexplosive.woodlib.impl.interfaces.Internal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * Loads and unloads {@link at.woodexplosive.woodlib.api.block.CustomBlockEntity CustomBlockEntities}
 * together with the chunks and worlds their cores are in.
 */
@Internal
public class CustomBlockEntityListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        CustomBlockEntityManager.loadChunk(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        CustomBlockEntityManager.unloadChunk(event.getChunk());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        CustomBlockEntityManager.unloadWorld(event.getWorld());
    }
}
