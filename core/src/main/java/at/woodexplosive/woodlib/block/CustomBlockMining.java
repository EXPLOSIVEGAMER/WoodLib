package at.woodexplosive.woodlib.block;

import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.ToolTier;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/**
 * Reimplements vanilla's block-breaking speed formula (see the "Breaking" article on the Minecraft
 * Wiki) for {@link ICustomBlock}s, since their parts are {@link Material#BARRIER Barrier} (or another
 * collision-only material) and never go through the server's own hardness/tool calculation - breaking
 * is driven entirely by {@link CustomBlockDigging} off the raw digging packets instead.
 */
final class CustomBlockMining {

    private CustomBlockMining() {}

    /**
     * @param customBlock the CustomBlock being mined
     * @param tool the item in the mining player's main hand
     * @return {@code true} if {@code tool} counts as the "correct tool" - required for full mining speed
     *         and for the broken structure to drop anything
     */
    static boolean isCorrectTool(@NotNull ICustomBlock customBlock, @NotNull ItemStack tool) {
        if (!isCorrectToolType(customBlock, tool)) return false;

        ToolTier minimum = customBlock.minimumToolTier();
        if (minimum == null) return true;

        ToolTier held = ToolTier.of(tool.getType());
        return held != null && held.level() >= minimum.level();
    }

    /**
     * @param customBlock the CustomBlock being mined
     * @param tool the item in the mining player's main hand
     * @return {@code true} if {@code tool} matches {@link ICustomBlock#requiredToolType()}, ignoring
     *         {@link ICustomBlock#minimumToolTier()}
     */
    static boolean isCorrectToolType(@NotNull ICustomBlock customBlock, @NotNull ItemStack tool) {
        Tag<Material> requiredType = customBlock.requiredToolType();
        if (requiredType == null) return true;
        return requiredType.isTagged(tool.getType());
    }


    /**
     * @param customBlock the CustomBlock being mined
     * @param player the mining player
     * @return the number of ticks {@code player} needs to hold down the mouse to break {@code customBlock},
     *         or {@code <= 0} if it should break instantly ({@link ICustomBlock#hardness()} not set)
     */
    static int breakTicks(@NotNull ICustomBlock customBlock, @NotNull Player player) {
        float hardness = customBlock.hardness();
        if (hardness <= 0f) return 0;

        ItemStack tool = player.getInventory().getItemInMainHand();

        float speed = isCorrectToolType(customBlock, tool) ? toolSpeed(tool) : 1f;
        speed = applyEfficiency(speed, tool);
        speed = applyHaste(speed, player);
        speed = applyMiningFatigue(speed, player);
        speed = applyEnvironment(speed, player);

        float damagePerTick = speed / hardness / (isCorrectTool(customBlock, tool) ? 30f : 100f);
        return (int) Math.ceil(1f / damagePerTick);
    }

    private static float toolSpeed(@NotNull ItemStack tool) {
        ToolTier tier = ToolTier.of(tool.getType());
        return tier != null ? tier.speed() : 1f;
    }

    private static float applyEfficiency(float speed, @NotNull ItemStack tool) {
        int level = tool.getEnchantmentLevel(Enchantment.EFFICIENCY);
        return level > 0 ? speed + (level * level + 1) : speed;
    }

    private static float applyHaste(float speed, @NotNull Player player) {
        PotionEffect haste = player.getPotionEffect(PotionEffectType.HASTE);
        return haste != null ? speed * (1f + 0.2f * (haste.getAmplifier() + 1)) : speed;
    }

    private static float applyMiningFatigue(float speed, @NotNull Player player) {
        PotionEffect fatigue = player.getPotionEffect(PotionEffectType.MINING_FATIGUE);
        if (fatigue == null) return speed;
        int level = Math.min(fatigue.getAmplifier() + 1, 4);
        return (float) (speed * Math.pow(0.3, level));
    }

    private static float applyEnvironment(float speed, @NotNull Player player) {
        // Player#isOnGround() is deprecated in favor of the Entity-level accessor it shadows.
        if (!((Entity) player).isOnGround()) speed /= 5f;
        if (player.isInWater() && !hasAquaAffinity(player)) speed /= 5f;
        return speed;
    }

    private static boolean hasAquaAffinity(@NotNull Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        return helmet != null && helmet.getEnchantmentLevel(Enchantment.AQUA_AFFINITY) > 0;
    }
}
