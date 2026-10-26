package at.woodexplosive.woodlib.impl.block;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.block.CustomBlockEntity;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.MultiblockInstance;
import at.woodexplosive.woodlib.impl.Scheduler;
import at.woodexplosive.woodlib.impl.interfaces.Internal;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Owns every loaded {@link CustomBlockEntity}: creates them on placement and chunk load, ticks them
 * every server tick, and unloads/removes them again - the counterpart of a world's TileEntity list.
 *
 * <p>Entities are keyed by their core's position. Everything runs on the main thread.</p>
 */
@Internal
public final class CustomBlockEntityManager {

    /** How often (in ticks) every entity's core is checked for still being a valid core. */
    private static final int VALIDATE_INTERVAL = 20;

    private static final Map<CoreKey, CustomBlockEntity> ENTITIES = new HashMap<>();
    private static int ticks = 0;

    private CustomBlockEntityManager() {}

    /**
     * Starts ticking and, one tick later, picks up the structures in every already-loaded chunk - late
     * enough for consumers to register their CustomBlocks in {@code onEnable} after {@code WoodLib.init}.
     * Call once from {@code WoodLib.init}.
     */
    public static void start() {
        Scheduler.repeat(CustomBlockEntityManager::tick, 1, 1);
        Scheduler.next(() -> {
            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) loadChunk(chunk);
            }
        });
    }

    /**
     * @param core a structure's core
     * @return its loaded entity, or {@code null}
     */
    public static @Nullable CustomBlockEntity get(@NotNull Block core) {
        return ENTITIES.get(CoreKey.of(core));
    }

    /**
     * @param cell any cell of a structure
     * @return the structure's loaded entity, or {@code null}
     */
    public static @Nullable CustomBlockEntity resolve(@NotNull Block cell) {
        CustomBlockRuntime.ResolvedBlock resolved = CustomBlockRuntime.resolve(cell, WoodLib.plugin());
        return resolved == null ? null : get(resolved.instance().core());
    }

    /**
     * Creates and loads the entity of {@code instance}, if its CustomBlock has one. An entity already
     * registered at the same core is unloaded first.
     * @param instance the placed structure
     */
    static void create(@NotNull MultiblockInstance instance) {
        CustomBlockEntity entity;
        try {
            entity = instance.customBlock().createEntity(instance);
        } catch (Exception e) {
            WoodLib.logger().error("Error while creating the CustomBlockEntity of {}", instance, e);
            return;
        }
        if (entity == null) return;

        CustomBlockEntity previous = ENTITIES.put(CoreKey.of(instance.core()), entity);
        if (previous != null) unload(previous);

        entity.setLoaded(true);
        call(entity, "onLoad", entity::onLoad);
    }

    /**
     * Removes the entity of a structure that is being broken, calling {@link CustomBlockEntity#onRemove()}.
     * @param instance the structure being broken
     */
    static void remove(@NotNull MultiblockInstance instance) {
        CustomBlockEntity entity = ENTITIES.remove(CoreKey.of(instance.core()));
        if (entity != null) remove(entity);
    }

    /**
     * Registers the cells in a freshly loaded chunk as known parts and creates the entity of every core in it.
     * @param chunk the loaded chunk
     */
    static void loadChunk(@NotNull Chunk chunk) {
        Plugin plugin = WoodLib.plugin();
        for (Block block : CustomBlockData.getBlocksWithCustomData(plugin, chunk)) {
            CustomBlockRuntime.Role role = CustomBlockRuntime.cellRole(block, plugin);
            if (role == null) continue;

            CustomBlockRuntime.addKnownPart(block);
            if (role != CustomBlockRuntime.Role.CORE) continue;

            CustomBlockRuntime.ResolvedBlock resolved = CustomBlockRuntime.resolve(block, plugin);
            if (resolved != null && get(block) == null) create(resolved.instance());
        }
    }

    /**
     * Unloads every entity whose core is in {@code chunk} and forgets the chunk's known parts.
     * @param chunk the unloading chunk
     */
    static void unloadChunk(@NotNull Chunk chunk) {
        UUID worldId = chunk.getWorld().getUID();
        int chunkX = chunk.getX(), chunkZ = chunk.getZ();
        unloadIf(key -> key.worldId().equals(worldId) && key.x() >> 4 == chunkX && key.z() >> 4 == chunkZ);

        Plugin plugin = WoodLib.plugin();
        for (Block block : CustomBlockData.getBlocksWithCustomData(plugin, chunk)) {
            if (CustomBlockRuntime.cellRole(block, plugin) != null) CustomBlockRuntime.removeKnownPart(block);
        }
    }

    /**
     * Unloads every entity in {@code world}.
     * @param world the unloading world
     */
    static void unloadWorld(@NotNull World world) {
        UUID worldId = world.getUID();
        unloadIf(key -> key.worldId().equals(worldId));
    }

    /** Unloads every entity. Called by {@code WoodLib.disable()}. */
    public static void unloadAll() {
        unloadIf(key -> true);
        ticks = 0;
    }

    private static void unloadIf(@NotNull Predicate<CoreKey> filter) {
        List<CustomBlockEntity> unloaded = new ArrayList<>();
        for (Iterator<Map.Entry<CoreKey, CustomBlockEntity>> it = ENTITIES.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<CoreKey, CustomBlockEntity> entry = it.next();
            if (!filter.test(entry.getKey())) continue;
            unloaded.add(entry.getValue());
            it.remove();
        }
        // Hooks run after the map is consistent, so they may safely query or change it.
        unloaded.forEach(CustomBlockEntityManager::unload);
    }

    /**
     * Ticks every loaded entity. Iterates over a copy, since a tick may place or break structures; an
     * exception in one entity is logged and doesn't stop the others. Every {@link #VALIDATE_INTERVAL}
     * ticks, entities whose core was replaced behind WoodLib's back (e.g. {@code /setblock}) are removed.
     */
    private static void tick() {
        boolean validate = ++ticks % VALIDATE_INTERVAL == 0;

        for (CustomBlockEntity entity : List.copyOf(ENTITIES.values())) {
            if (!entity.isLoaded()) continue;
            if (validate && !isValidCore(entity)) {
                ENTITIES.remove(CoreKey.of(entity.core()), entity);
                remove(entity);
                continue;
            }
            call(entity, "tick", entity::tick);
        }
    }

    private static boolean isValidCore(@NotNull CustomBlockEntity entity) {
        CustomBlockRuntime.ResolvedBlock resolved = CustomBlockRuntime.resolve(entity.core(), WoodLib.plugin());
        ICustomBlock expected = entity.instance().customBlock();
        return resolved != null && resolved.role() == CustomBlockRuntime.Role.CORE
                && resolved.instance().customBlock().id().equals(expected.id());
    }

    private static void unload(@NotNull CustomBlockEntity entity) {
        entity.setLoaded(false);
        call(entity, "onUnload", entity::onUnload);
    }

    private static void remove(@NotNull CustomBlockEntity entity) {
        entity.setLoaded(false);
        call(entity, "onRemove", entity::onRemove);
    }

    private static void call(@NotNull CustomBlockEntity entity, @NotNull String hook, @NotNull Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            WoodLib.logger().error("Error in {}#{}", entity, hook, e);
        }
    }

    /** A core position scoped to its world. */
    private record CoreKey(@NotNull UUID worldId, int x, int y, int z) {
        static CoreKey of(@NotNull Block core) {
            return new CoreKey(core.getWorld().getUID(), core.getX(), core.getY(), core.getZ());
        }
    }
}
