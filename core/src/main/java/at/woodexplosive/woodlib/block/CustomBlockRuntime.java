package at.woodexplosive.woodlib.block;

import at.woodexplosive.woodlib.WoodLib;
import at.woodexplosive.woodlib.api.block.CustomBlockPart;
import at.woodexplosive.woodlib.api.block.CustomBlockRegistry;
import at.woodexplosive.woodlib.api.block.DisplayDefinition;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;

/**
 * Stateless world-mutation and rotation helpers for the CustomBlock system, kept separate from
 * {@link CustomBlockListener} so the listener only deals with Bukkit event wiring/gating.
 */
final class CustomBlockRuntime {

    /** Extra padding (in blocks) around a structure's part cells when scanning for its display entities. */
    private static final double DISPLAY_SCAN_PADDING = 1.5;

    private CustomBlockRuntime() {}

    // ---- Rotation ----

    /**
     * Snaps a player's yaw to the nearest of the 4 cardinal rotation steps (0-3, 90° each).
     *
     * <p>Offset by 2 steps (180°) because {@code rotationSteps == 0} must line up with how
     * directional {@code BlockData} defaults when left unset (facing north, e.g. an unrotated chest),
     * not with the raw compass direction the placing player faces - without this offset every
     * structure comes out rotated a consistent 180° from where it should be.</p>
     *
     * @param yaw the player's yaw
     * @return the rotation step, 0-3
     */
    static int yawToRotationSteps(float yaw) {
        float normalized = ((yaw % 360f) + 360f) % 360f;
        return (Math.round(normalized / 90f) + 2) % 4;
    }

    /**
     * Rotates an integer part offset around the origin by {@code steps} 90° increments (Y axis).
     * @param offset the offset to rotate
     * @param steps the rotation, in 90° steps
     * @return the rotated offset
     */
    static @NotNull BlockVector rotateOffset(@NotNull BlockVector offset, int steps) {
        int x = offset.getBlockX();
        int z = offset.getBlockZ();
        for (int i = 0; i < normalizeSteps(steps); i++) {
            int nx = -z;
            int nz = x;
            x = nx;
            z = nz;
        }
        return new BlockVector(x, offset.getBlockY(), z);
    }

    /**
     * Rotates a display {@link Transformation} by {@code steps} 90° increments around the block's
     * local vertical axis (pivoting translation around the block's center, {@code (0.5, y, 0.5)}, so a
     * display offset within its cell still lands inside the rotated cell).
     *
     * <p>The angle is negated because JOML's {@code rotateY} turns counter-clockwise (viewed from
     * above) for a positive angle, while {@link #rotateOffset} turns part positions clockwise -
     * negating keeps the visual rotation consistent with where the parts actually get placed. Note
     * that this only rotates displays <i>relative to their authored orientation</i> - a display whose
     * own {@code BlockData} carries a baked-in facing (e.g. a chest, which defaults to north when
     * unset) must be authored facing south so it lines up with {@code rotationSteps == 0} (the
     * placing player facing south); otherwise the visible result is off by 180° specifically on the
     * north/south axis, since a north-authored default only happens to cancel out on the east/west axis.</p>
     *
     * @param transformation the authored transformation
     * @param steps the rotation, in 90° steps
     * @return the rotated transformation
     */
    static @NotNull Transformation rotateTransformation(@NotNull Transformation transformation, int steps) {
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
     * @param steps a rotation in 90° steps, possibly negative or {@code >= 4}
     * @return the equivalent rotation normalized to {@code 0-3}
     */
    private static int normalizeSteps(int steps) {
        return ((steps % 4) + 4) % 4;
    }

    // ---- Placement validation ----

    /**
     * @param block the block to check
     * @return {@code true} if a CustomBlock part may be placed there (air only, for v1)
     */
    static boolean isPlaceable(@NotNull Block block) {
        return block.getType().isAir();
    }

    /**
     * @param customBlock the CustomBlock being placed
     * @param origin the placement origin
     * @param rotationSteps the rotation to place at
     * @return {@code true} if every part's target block is {@link #isPlaceable(Block)}
     */
    static boolean allTargetsPlaceable(@NotNull ICustomBlock customBlock, @NotNull Block origin, int rotationSteps) {
        for (CustomBlockPart part : customBlock.parts()) {
            if (!isPlaceable(partBlock(origin, part, rotationSteps))) return false;
        }
        return true;
    }

    // ---- World mutation ----

    /**
     * Places every part of {@code customBlock}: sets its barrier material, tags its
     * {@link CustomBlockData}, and spawns its {@link DisplayDefinition}s.
     * @param customBlock the CustomBlock to place
     * @param origin the placement origin
     * @param rotationSteps the rotation to place at
     */
    static void placeParts(@NotNull ICustomBlock customBlock, @NotNull Block origin, int rotationSteps) {
        Plugin plugin = WoodLib.plugin();
        List<CustomBlockPart> parts = customBlock.parts();

        for (int i = 0; i < parts.size(); i++) {
            CustomBlockPart part = parts.get(i);
            Block target = partBlock(origin, part, rotationSteps);
            target.setType(part.barrierMaterial(), false);

            CustomBlockData data = new CustomBlockData(target, plugin);
            data.set(ICustomBlock.idKey(), PersistentDataType.STRING, customBlock.id().asString());
            data.set(CustomBlockKeys.origin(), PersistentDataType.INTEGER_ARRAY, originArray(origin));
            data.set(CustomBlockKeys.rotation(), PersistentDataType.INTEGER, rotationSteps);
            data.set(CustomBlockKeys.partIndex(), PersistentDataType.INTEGER, i);

            for (DisplayDefinition display : part.displays()) {
                spawnDisplay(target, origin, customBlock.id(), display, rotationSteps);
            }
        }
    }

    /**
     * Removes every part of {@code customBlock}: clears its barrier material and
     * {@link CustomBlockData}, and despawns its display entities.
     * @param customBlock the CustomBlock to remove
     * @param origin the structure's placement origin
     * @param rotationSteps the rotation it was placed at
     */
    static void removeParts(@NotNull ICustomBlock customBlock, @NotNull Block origin, int rotationSteps) {
        Plugin plugin = WoodLib.plugin();

        for (CustomBlockPart part : customBlock.parts()) {
            Block target = partBlock(origin, part, rotationSteps);
            target.setType(Material.AIR, false);
            new CustomBlockData(target, plugin).clear();
        }

        removeDisplays(customBlock, origin, rotationSteps);
    }

    /**
     * Spawns a single {@link BlockDisplay} for {@code display} at {@code target}'s location, rotated
     * for {@code rotationSteps}, and tags it with {@code customBlockId} and {@code origin} so
     * {@link #removeDisplays} can find it again.
     * @param target the part block the display is spawned at
     * @param origin the structure's placement origin (stamped on the entity for later lookup)
     * @param customBlockId the owning CustomBlock's id (stamped on the entity for later lookup)
     * @param display the display's visual configuration
     * @param rotationSteps the rotation the structure was placed at
     */
    private static void spawnDisplay(@NotNull Block target, @NotNull Block origin, @NotNull NamespacedKey customBlockId,
                                      @NotNull DisplayDefinition display, int rotationSteps) {
        Location location = target.getLocation();
        target.getWorld().spawn(location, BlockDisplay.class, entity -> {
            entity.setBlock(display.blockData());
            entity.setTransformation(rotateTransformation(display.transformation(), rotationSteps));
            entity.setBillboard(display.billboard());
            if (display.brightness() != null) entity.setBrightness(display.brightness());
            entity.setGlowing(display.glowing());
            if (display.glowColorOverride() != null) entity.setGlowColorOverride(display.glowColorOverride());
            entity.setGravity(false);
            entity.setPersistent(true);
            entity.setInvulnerable(true);

            PersistentDataContainer pdc = entity.getPersistentDataContainer();
            pdc.set(ICustomBlock.idKey(), PersistentDataType.STRING, customBlockId.asString());
            pdc.set(CustomBlockKeys.origin(), PersistentDataType.INTEGER_ARRAY, originArray(origin));
        });
    }

    /**
     * Finds and removes every {@link BlockDisplay} tagged with {@code customBlock}'s id and
     * {@code origin} within the structure's {@link #structureBounds(Block, ICustomBlock, int)}, rather
     * than tracking spawned entity UUIDs - self-healing against external entity removal/desync.
     * @param customBlock the CustomBlock structure being removed
     * @param origin the structure's placement origin
     * @param rotationSteps the rotation it was placed at
     */
    private static void removeDisplays(@NotNull ICustomBlock customBlock, @NotNull Block origin, int rotationSteps) {
        BoundingBox bounds = structureBounds(origin, customBlock, rotationSteps);
        int[] originArray = originArray(origin);

        for (Entity entity : origin.getWorld().getNearbyEntities(bounds)) {
            if (!(entity instanceof BlockDisplay)) continue;

            PersistentDataContainer pdc = entity.getPersistentDataContainer();
            String taggedId = pdc.get(ICustomBlock.idKey(), PersistentDataType.STRING);
            int[] taggedOrigin = pdc.get(CustomBlockKeys.origin(), PersistentDataType.INTEGER_ARRAY);

            if (customBlock.id().asString().equals(taggedId) && Arrays.equals(originArray, taggedOrigin)) {
                entity.remove();
            }
        }
    }

    /**
     * @param origin the structure's placement origin
     * @param customBlock the CustomBlock definition
     * @param rotationSteps the rotation it was (or would be) placed at
     * @return a bounding box covering every part's 1x1x1 cell, padded to also cover their displays
     */
    static @NotNull BoundingBox structureBounds(@NotNull Block origin, @NotNull ICustomBlock customBlock, int rotationSteps) {
        BoundingBox bounds = null;
        for (CustomBlockPart part : customBlock.parts()) {
            Block target = partBlock(origin, part, rotationSteps);
            BoundingBox cell = new BoundingBox(target.getX(), target.getY(), target.getZ(),
                    target.getX() + 1, target.getY() + 1, target.getZ() + 1);
            bounds = bounds == null ? cell : bounds.union(cell);
        }
        assert bounds != null : "ICustomBlock.parts() must never be empty";
        return bounds.expand(DISPLAY_SCAN_PADDING);
    }

    // ---- Resolution ----

    /**
     * Resolves the CustomBlock structure a clicked part block belongs to, from the tags stamped on
     * its {@link CustomBlockData}.
     * @param clicked the clicked part block
     * @param plugin the owning plugin
     * @return the resolved structure info, or {@code null} if {@code clicked} isn't a known/registered
     *         CustomBlock part
     */
    static @Nullable ResolvedPart resolve(@NotNull Block clicked, @NotNull Plugin plugin) {
        if (!CustomBlockData.hasCustomBlockData(clicked, plugin)) return null;

        CustomBlockData data = new CustomBlockData(clicked, plugin);
        String stringId = data.get(ICustomBlock.idKey(), PersistentDataType.STRING);
        int[] origin = data.get(CustomBlockKeys.origin(), PersistentDataType.INTEGER_ARRAY);
        Integer rotation = data.get(CustomBlockKeys.rotation(), PersistentDataType.INTEGER);
        Integer partIndex = data.get(CustomBlockKeys.partIndex(), PersistentDataType.INTEGER);

        if (stringId == null || origin == null || origin.length != 3 || rotation == null || partIndex == null) return null;

        NamespacedKey id = NamespacedKey.fromString(stringId);
        if (id == null) return null;

        ICustomBlock customBlock = CustomBlockRegistry.get(id);
        if (customBlock == null) return null;

        Block originBlock = clicked.getWorld().getBlockAt(origin[0], origin[1], origin[2]);
        return new ResolvedPart(customBlock, originBlock, rotation, partIndex);
    }

    /**
     * @param origin the structure's placement origin
     * @param part the part whose absolute block position is needed
     * @param rotationSteps the rotation to apply to the part's offset
     * @return the part's absolute, rotated block position
     */
    private static @NotNull Block partBlock(@NotNull Block origin, @NotNull CustomBlockPart part, int rotationSteps) {
        BlockVector rotated = rotateOffset(part.offset(), rotationSteps);
        return origin.getRelative(rotated.getBlockX(), rotated.getBlockY(), rotated.getBlockZ());
    }

    /**
     * @param origin the structure's placement origin
     * @return {@code origin}'s coordinates as {@code [x, y, z]}, for stamping into persistent data
     */
    private static int[] originArray(@NotNull Block origin) {
        return new int[]{origin.getX(), origin.getY(), origin.getZ()};
    }

    /**
     * A CustomBlock structure resolved from one of its clicked part blocks.
     * @param customBlock the resolved {@link ICustomBlock} definition
     * @param originBlock the structure's placement origin block
     * @param rotationSteps the rotation the structure was placed at
     * @param partIndex the clicked block's index into {@link ICustomBlock#parts()}
     */
    record ResolvedPart(@NotNull ICustomBlock customBlock, @NotNull Block originBlock, int rotationSteps, int partIndex) {}
}
