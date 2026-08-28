package at.woodexplosive.woodlib.api.block;

import org.bukkit.Material;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Vanilla tool material tiers, used by {@link ICustomBlock#minimumToolTier()} to gate "correct tool for
 * drops" and by the mining-speed formula to look up a held tool's base speed.
 *
 * <p>{@link #level()} is the vanilla harvest level {@code minimumToolTier()} compares against;
 * {@link #speed()} is the vanilla base mining speed multiplier for a correct tool of this material. Gold
 * sits at the same harvest level as wood but mines much faster, exactly like vanilla gold tools.</p>
 *
 * <p>Membership is matched against an explicit {@link Material} set per tier, not a {@link org.bukkit.Tag}
 * - Bukkit's {@code Tag.ITEMS_*_TOOL_MATERIALS} constants are the vanilla {@code *_tool_materials} tags,
 * which list the crafting *ingredient* (e.g. {@code minecraft:iron_tool_materials} contains only
 * {@code iron_ingot}), not the finished tools. There's no vanilla tag grouping "all iron tools", so this
 * enumerates the five tool types per tier directly.</p>
 */
public enum ToolTier {
    WOOD(0, 2f, Material.WOODEN_PICKAXE, Material.WOODEN_AXE, Material.WOODEN_SHOVEL, Material.WOODEN_HOE, Material.WOODEN_SWORD),
    GOLD(0, 12f, Material.GOLDEN_PICKAXE, Material.GOLDEN_AXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_HOE, Material.GOLDEN_SWORD),
    COPPER(1, 5f, Material.COPPER_PICKAXE, Material.COPPER_AXE, Material.COPPER_SHOVEL, Material.COPPER_HOE, Material.COPPER_SWORD),
    STONE(1, 4f, Material.STONE_PICKAXE, Material.STONE_AXE, Material.STONE_SHOVEL, Material.STONE_HOE, Material.STONE_SWORD),
    IRON(2, 6f, Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SHOVEL, Material.IRON_HOE, Material.IRON_SWORD),
    DIAMOND(3, 8f, Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE, Material.DIAMOND_SWORD),
    NETHERITE(4, 9f, Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE, Material.NETHERITE_SWORD);

    private final int level;
    private final float speed;
    private final Set<Material> materials;

    ToolTier(int level, float speed, @NotNull Material... materials) {
        this.level = level;
        this.speed = speed;
        this.materials = Set.of(materials);
    }

    /** @return the vanilla harvest level (wood/gold = 0, stone/copper = 1, iron = 2, diamond = 3, netherite = 4) */
    @Contract(pure = true)
    public int level() {
        return level;
    }

    /** @return the vanilla base mining speed multiplier for a correct tool of this material */
    @Contract(pure = true)
    public float speed() {
        return speed;
    }

    /**
     * @param toolMaterial a held item's material
     * @return the {@link ToolTier} it belongs to (any pickaxe/axe/shovel/hoe/sword of that material), or
     *         {@code null} if it isn't a modeled vanilla tool material
     */
    @Contract(pure = true)
    public static @Nullable ToolTier of(@NotNull Material toolMaterial) {
        for (ToolTier tier : values()) {
            if (tier.materials.contains(toolMaterial)) return tier;
        }
        return null;
    }
}
