package at.woodexplosive.woodlib.impl.block;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.block.CustomBlockRegistry;
import at.woodexplosive.woodlib.api.block.Dimensions;
import at.woodexplosive.woodlib.api.block.DisplayDefinition;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.LocalDirection;
import at.woodexplosive.woodlib.api.block.LocalPos;
import at.woodexplosive.woodlib.api.block.MultiblockInstance;
import at.woodexplosive.woodlib.api.block.PlacementContext;
import at.woodexplosive.woodlib.impl.interfaces.Internal;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stateless world-mutation, placement and rotation helpers for the CustomBlock system, kept separate
 * from {@link CustomBlockListener} so the listener only deals with Bukkit event wiring/gating.
 *
 * <p>Follows HBM NTM's dummyable model: the core cell stores the CustomBlock id and facing, every other
 * cell (dummy or port) stores the id and the core's position. Resolving any cell therefore always goes
 * through the core.</p>
 */
@Internal
final class CustomBlockRuntime {

    /** Extra padding (in blocks) around a structure's cells when scanning for its display entities. */
    private static final double DISPLAY_SCAN_PADDING = 1.5;

    /**
     * Thread-safe set of every currently-placed cell's location, so {@link CustomBlockDigging}'s packet
     * listener can synchronously tell "is this a CustomBlock cell" from the Netty thread - packet
     * cancellation has to happen before {@code onPacketReceive} returns, which is too early to hop to the
     * main thread and back. {@link #resolve} can't be used there since it reads {@link CustomBlockData},
     * which (like the rest of the Bukkit API) must stay on the main thread.
     *
     * <p>Populated in {@link #place}, cleared in {@link #remove} - both main-thread-only.
     * {@link #isKnownPart} itself is safe to call from any thread. Not backfilled from persisted
     * {@link CustomBlockData} on world/chunk load, so a CustomBlock placed in a previous server session
     * won't get packet-level cancellation until re-placed; the normal, {@link CustomBlockData}-backed
     * {@link #resolve} path is unaffected and still works for it regardless.</p>
     */
    private static final Set<PartKey> KNOWN_PARTS = ConcurrentHashMap.newKeySet();

    private CustomBlockRuntime() {}

    /** A cell's role in its structure. Persisted by ordinal - only append. */
    enum Role {
        CORE, DUMMY, PORT
    }

    // ---- Rotation ----

    /**
     * Rotates a display {@link Transformation} by {@code steps} 90° increments around the block's
     * local vertical axis (pivoting translation around the block's center, {@code (0.5, y, 0.5)}, so a
     * display offset within its cell still lands inside the rotated cell).
     *
     * <p>The angle is negated because JOML's {@code rotateY} turns counter-clockwise (viewed from
     * above) for a positive angle, while {@link MultiblockInstance#toWorld(LocalPos)} turns cell positions
     * clockwise - negating keeps the visual rotation consistent with where the cells actually get placed.</p>
     *
     * @param transformation the authored transformation
     * @param steps the rotation, in 90° steps
     * @return the rotated transformation
     */
    static @NotNull Transformation createTransformation(@NotNull Transformation transformation, int steps) {
        int normalized = normalizeSteps(steps);
        if (normalized == 0) return transformation;

        Quaternionf yRot = new Quaternionf().rotateY((float) Math.toRadians(-normalized * 90.0));

        Vector3f translation = new Vector3f(transformation.getTranslation()).sub(0.5f, 0f, 0.5f);
        yRot.transform(translation);
        translation.add(0.5f, 0f, 0.5f);

        Quaternionf leftRotation = yRot.mul(new Quaternionf(transformation.getLeftRotation()), new Quaternionf());

        return new Transformation(translation, leftRotation, new Vector3f(transformation.getScale()),
                new Quaternionf(transformation.getRightRotation()));
    }

    /**
     * Rotates a cell's collision {@link BlockData} (e.g. a stair's facing) by {@code steps} 90°
     * increments, clockwise viewed from above - the same direction cell positions turn, so the block
     * state stays aligned with where the cell gets placed.
     * @param data the block data to rotate in place
     * @param steps the rotation, in 90° steps
     * @return {@code data}, for chaining
     */
    static @NotNull BlockData rotateBlockData(@NotNull BlockData data, int steps) {
        switch (normalizeSteps(steps)) {
            case 1 -> data.rotate(StructureRotation.CLOCKWISE_90);
            case 2 -> data.rotate(StructureRotation.CLOCKWISE_180);
            case 3 -> data.rotate(StructureRotation.COUNTERCLOCKWISE_90);
            default -> { }
        }
        return data;
    }

    /**
     * @param steps a rotation in 90° steps, possibly negative or {@code >= 4}
     * @return the equivalent rotation normalized to {@code 0-3}
     */
    private static int normalizeSteps(int steps) {
        return ((steps % 4) + 4) % 4;
    }

    // ---- Placement ----

    /**
     * Works out where and how {@code customBlock} ends up when {@code player} right-clicks
     * {@code clickedFace} of {@code clickedBlock} - the counterpart of HBM's
     * {@code BlockDummyable#onBlockPlacedBy} positioning logic:
     * <ul>
     *     <li>The facing is the clicked side face, or (on a floor/ceiling) the direction towards the
     *     player, then passed through {@link ICustomBlock#modifyFacing}. Non-rotatable blocks always
     *     face north.</li>
     *     <li>On a floor/ceiling the core is pushed {@link ICustomBlock#offset()} blocks away from the
     *     player; the structure's bottom (floor) or top (ceiling) lands on the aimed-at block.</li>
     *     <li>On a wall the core is pushed out of the wall so the structure sits flush against it, its
     *     bottom on the aimed-at block's level.</li>
     * </ul>
     * {@link ICustomBlock#heightOffset()} is added on top in every case.
     * @param customBlock the CustomBlock being placed
     * @param player the placing player
     * @param clickedBlock the clicked block
     * @param clickedFace the clicked face
     * @return the resolved placement, not yet checked with {@link ICustomBlock#canPlace}
     */
    static @NotNull PlacementContext computePlacement(@NotNull ICustomBlock customBlock, @NotNull Player player,
                                                      @NotNull Block clickedBlock, @NotNull BlockFace clickedFace) {
        Block target = clickedBlock.getRelative(clickedFace);
        boolean vertical = clickedFace == BlockFace.UP || clickedFace == BlockFace.DOWN;

        BlockFace facing = BlockFace.NORTH;
        if (customBlock.rotatable()) {
            facing = vertical ? player.getFacing().getOppositeFace() : clickedFace;
            facing = customBlock.modifyFacing(facing);
        }

        Dimensions bounds = customBlock.shape().bounds();
        MultiblockInstance atTarget = new MultiblockInstance(customBlock, target, facing);
        Block core;

        if (vertical) {
            int offset = customBlock.offset();
            BlockFace away = player.getFacing();
            int dy = clickedFace == BlockFace.UP ? bounds.down() : -bounds.up();
            core = target.getRelative(away.getModX() * offset, dy, away.getModZ() * offset);
        } else {
            int depth = bounds.extent(localDirectionOf(atTarget, clickedFace.getOppositeFace()));
            core = target.getRelative(clickedFace.getModX() * depth, bounds.down(), clickedFace.getModZ() * depth);
        }
        core = core.getRelative(0, customBlock.heightOffset(), 0);

        return new PlacementContext(player, clickedBlock, clickedFace, new MultiblockInstance(customBlock, core, facing));
    }

    /**
     * @param instance any instance with the wanted facing
     * @param worldFace a world direction
     * @return the local direction that maps onto {@code worldFace} for {@code instance}'s facing
     */
    private static @NotNull LocalDirection localDirectionOf(@NotNull MultiblockInstance instance, @NotNull BlockFace worldFace) {
        for (LocalDirection direction : LocalDirection.values()) {
            if (instance.toWorld(direction) == worldFace) return direction;
        }
        throw new IllegalArgumentException("Not an axis direction: " + worldFace);
    }

    // ---- World mutation ----

    /**
     * Places {@code instance}: the core first (id, facing, displays), then every dummy and port pointing
     * at it - HBM's {@code fillSpace} plus {@code makeExtra}.
     * @param instance the structure to place
     */
    static void place(@NotNull MultiblockInstance instance) {
        Plugin plugin = WoodLib.plugin();
        ICustomBlock customBlock = instance.customBlock();
        int steps = instance.rotationSteps();
        String id = customBlock.id().asString();
        int[] coreArray = coreArray(instance.core());

        for (LocalPos pos : customBlock.shape().cells()) {
            Block target = instance.toWorld(pos);
            target.setBlockData(rotateBlockData(customBlock.collisionData(pos), steps), false);

            CustomBlockData data = new CustomBlockData(target, plugin);
            data.set(ICustomBlock.idKey(), PersistentDataType.STRING, id);

            Role role;
            if (pos.equals(LocalPos.CORE)) {
                role = Role.CORE;
                data.set(CustomBlockKeys.facing(), PersistentDataType.STRING, instance.facing().name());
            } else {
                role = customBlock.shape().portsAt(pos).isEmpty() ? Role.DUMMY : Role.PORT;
                data.set(CustomBlockKeys.core(), PersistentDataType.INTEGER_ARRAY, coreArray);
            }
            data.set(CustomBlockKeys.role(), PersistentDataType.BYTE, (byte) role.ordinal());

            KNOWN_PARTS.add(PartKey.of(target));
        }

        for (DisplayDefinition display : customBlock.displays()) {
            spawnDisplay(instance, display);
        }
    }

    /**
     * Removes {@code instance}: clears every cell still tagged with its id, and despawns its displays.
     * @param instance the structure to remove
     */
    static void remove(@NotNull MultiblockInstance instance) {
        Plugin plugin = WoodLib.plugin();
        String id = instance.customBlock().id().asString();

        for (Block target : instance.cells()) {
            KNOWN_PARTS.remove(PartKey.of(target));
            if (!CustomBlockData.hasCustomBlockData(target, plugin)) continue;

            CustomBlockData data = new CustomBlockData(target, plugin);
            if (!id.equals(data.get(ICustomBlock.idKey(), PersistentDataType.STRING))) continue;

            target.setType(Material.AIR, false);
            data.clear();
        }

        removeDisplays(instance);
    }

    /**
     * Clears a single cell that no longer belongs to a valid structure (HBM's {@code destroyIfOrphan}).
     * @param block the orphaned cell
     */
    static void removeOrphan(@NotNull Block block) {
        KNOWN_PARTS.remove(PartKey.of(block));
        new CustomBlockData(block, WoodLib.plugin()).clear();
        block.setType(Material.AIR, false);
    }

    /**
     * Thread-safe check for whether {@code (x, y, z)} in the world identified by {@code worldId} is a
     * currently-placed CustomBlock cell. Safe to call from any thread, unlike {@link #resolve}.
     * @param worldId the world's unique id
     * @param x block x
     * @param y block y
     * @param z block z
     * @return {@code true} if a cell is currently placed there
     */
    static boolean isKnownPart(@NotNull UUID worldId, int x, int y, int z) {
        return KNOWN_PARTS.contains(new PartKey(worldId, x, y, z));
    }

    /** Composite key for {@link #KNOWN_PARTS} - a block position scoped to its world. */
    private record PartKey(@NotNull UUID worldId, int x, int y, int z) {
        static PartKey of(@NotNull Block block) {
            return new PartKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }
    }

    /**
     * Spawns the display entity for {@code display} at the core - a {@link BlockDisplay} for
     * {@link DisplayDefinition.OfBlock}, an {@link ItemDisplay} for {@link DisplayDefinition.OfItem} -
     * rotated for the structure's facing, and tags it with the CustomBlock id and core position so
     * {@link #removeDisplays} can find it again.
     * @param instance the structure the display belongs to
     * @param display the display's visual configuration
     */
    private static void spawnDisplay(@NotNull MultiblockInstance instance, @NotNull DisplayDefinition display) {
        Block core = instance.core();
        Location location = core.getLocation();
        switch (display) {
            case DisplayDefinition.OfBlock ofBlock -> core.getWorld().spawn(location, BlockDisplay.class, entity -> {
                entity.setBlock(ofBlock.blockData());
                configureCommon(entity, display, instance);
            });
            case DisplayDefinition.OfItem ofItem -> core.getWorld().spawn(location, ItemDisplay.class, entity -> {
                entity.setItemStack(ofItem.itemStack());
                entity.setItemDisplayTransform(ofItem.itemTransform());
                configureCommon(entity, display, instance);
            });
        }
    }

    /**
     * Applies the transform/billboard/brightness/glow/physics config shared by every display type, and
     * stamps the PDC tags {@link #removeDisplays} looks up entities by.
     * @param entity the spawned {@link BlockDisplay} or {@link ItemDisplay}
     * @param display the display's visual configuration
     * @param instance the structure the display belongs to
     */
    private static void configureCommon(@NotNull Display entity, @NotNull DisplayDefinition display, @NotNull MultiblockInstance instance) {
        entity.setTransformation(createTransformation(display.transformation(), instance.rotationSteps()));
        entity.setBillboard(display.billboard());
        if (display.brightness() != null) entity.setBrightness(display.brightness());
        entity.setGlowing(display.glowing());
        if (display.glowColorOverride() != null) entity.setGlowColorOverride(display.glowColorOverride());
        entity.setGravity(false);
        entity.setPersistent(true);
        entity.setInvulnerable(true);

        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(ICustomBlock.idKey(), PersistentDataType.STRING, instance.customBlock().id().asString());
        pdc.set(CustomBlockKeys.core(), PersistentDataType.INTEGER_ARRAY, coreArray(instance.core()));
    }

    /**
     * Finds and removes every display entity tagged with {@code instance}'s id and core within the
     * structure's {@link #structureBounds bounds}, rather than tracking spawned entity UUIDs -
     * self-healing against external entity removal/desync.
     * @param instance the structure being removed
     */
    private static void removeDisplays(@NotNull MultiblockInstance instance) {
        String id = instance.customBlock().id().asString();
        int[] coreArray = coreArray(instance.core());

        for (Entity entity : instance.core().getWorld().getNearbyEntities(structureBounds(instance))) {
            if (!(entity instanceof Display)) continue;

            PersistentDataContainer pdc = entity.getPersistentDataContainer();
            String taggedId = pdc.get(ICustomBlock.idKey(), PersistentDataType.STRING);
            int[] taggedCore = pdc.get(CustomBlockKeys.core(), PersistentDataType.INTEGER_ARRAY);

            if (id.equals(taggedId) && Arrays.equals(coreArray, taggedCore)) {
                entity.remove();
            }
        }
    }

    /**
     * @param instance the structure
     * @return a bounding box covering every cell, padded to also cover their displays
     */
    static @NotNull BoundingBox structureBounds(@NotNull MultiblockInstance instance) {
        BoundingBox bounds = null;
        for (Block cell : instance.cells()) {
            BoundingBox box = BoundingBox.of(cell);
            bounds = bounds == null ? box : bounds.union(box);
        }
        assert bounds != null : "MultiblockShape.cells() always contains the core";
        return bounds.expand(DISPLAY_SCAN_PADDING);
    }

    // ---- Resolution ----

    /**
     * Resolves the CustomBlock structure a cell belongs to (HBM's {@code findCore}). A dummy whose core
     * is gone or no longer matches is an orphan and gets removed on the spot - unless the core's chunk
     * isn't loaded, in which case nothing is touched (HBM's chunk-border precaution).
     * @param block the cell
     * @param plugin the owning plugin
     * @return the resolved structure, or {@code null} if {@code block} isn't a valid CustomBlock cell
     */
    static @Nullable ResolvedBlock resolve(@NotNull Block block, @NotNull Plugin plugin) {
        if (!CustomBlockData.hasCustomBlockData(block, plugin)) return null;

        CustomBlockData data = new CustomBlockData(block, plugin);
        NamespacedKey id = readId(data);
        Role role = readRole(data);
        if (id == null || role == null) return null;

        ICustomBlock customBlock = CustomBlockRegistry.get(id);
        if (customBlock == null) return null;

        if (role == Role.CORE) {
            BlockFace facing = readFacing(data);
            if (facing == null) return null;
            return new ResolvedBlock(new MultiblockInstance(customBlock, block, facing), role);
        }

        int[] coreArray = data.get(CustomBlockKeys.core(), PersistentDataType.INTEGER_ARRAY);
        if (coreArray == null || coreArray.length != 3) return null;

        World world = block.getWorld();
        if (!world.isChunkLoaded(coreArray[0] >> 4, coreArray[2] >> 4)) return null;

        Block core = world.getBlockAt(coreArray[0], coreArray[1], coreArray[2]);
        BlockFace facing = null;
        if (CustomBlockData.hasCustomBlockData(core, plugin)) {
            CustomBlockData coreData = new CustomBlockData(core, plugin);
            if (id.equals(readId(coreData)) && readRole(coreData) == Role.CORE) facing = readFacing(coreData);
        }

        if (facing == null) {
            removeOrphan(block);
            return null;
        }
        return new ResolvedBlock(new MultiblockInstance(customBlock, core, facing), role);
    }

    private static @Nullable NamespacedKey readId(@NotNull CustomBlockData data) {
        String stringId = data.get(ICustomBlock.idKey(), PersistentDataType.STRING);
        return stringId == null ? null : NamespacedKey.fromString(stringId);
    }

    private static @Nullable Role readRole(@NotNull CustomBlockData data) {
        Byte ordinal = data.get(CustomBlockKeys.role(), PersistentDataType.BYTE);
        if (ordinal == null || ordinal < 0 || ordinal >= Role.values().length) return null;
        return Role.values()[ordinal];
    }

    private static @Nullable BlockFace readFacing(@NotNull CustomBlockData data) {
        String name = data.get(CustomBlockKeys.facing(), PersistentDataType.STRING);
        if (name == null) return null;
        try {
            return BlockFace.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * @param core the structure's core
     * @return {@code core}'s coordinates as {@code [x, y, z]}, for stamping into persistent data
     */
    private static int[] coreArray(@NotNull Block core) {
        return new int[] { core.getX(), core.getY(), core.getZ() };
    }

    /**
     * A CustomBlock structure resolved from one of its cells.
     * @param instance the structure
     * @param role the resolved cell's role in it
     */
    record ResolvedBlock(@NotNull MultiblockInstance instance, @NotNull Role role) {}
}
