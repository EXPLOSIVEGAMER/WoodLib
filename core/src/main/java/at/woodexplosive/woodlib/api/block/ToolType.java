package at.woodexplosive.woodlib.api.block;

import org.bukkit.Material;
import org.bukkit.Tag;

/**
 * Convenience aliases for the vanilla item {@link Tag}s commonly passed to
 * {@link at.woodexplosive.woodlib.api.block.builder.ICustomBlockBuilder#requiredToolType(Tag)} /
 * {@link ICustomBlock#requiredToolType()}.
 */
public interface ToolType {
    /** Any pickaxe. */
    Tag<Material> PICKAXES = Tag.ITEMS_PICKAXES;
    /** Any axe. */
    Tag<Material> AXES = Tag.ITEMS_AXES;
    /** Any shovel. */
    Tag<Material> SHOVEL = Tag.ITEMS_SHOVELS;
    /** Any hoe. */
    Tag<Material> HOE = Tag.ITEMS_HOES;
    /** Any sword. */
    Tag<Material> SWORD = Tag.ITEMS_SWORDS;
}
