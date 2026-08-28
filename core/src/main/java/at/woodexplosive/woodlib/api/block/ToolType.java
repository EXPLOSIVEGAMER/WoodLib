package at.woodexplosive.woodlib.api.block;

import org.bukkit.Material;
import org.bukkit.Tag;

public interface ToolType {
    Tag<Material> PICKAXES = Tag.ITEMS_PICKAXES;
    Tag<Material> AXES = Tag.ITEMS_AXES;
    Tag<Material> SHOVEL = Tag.ITEMS_SHOVELS;
    Tag<Material> HOE = Tag.ITEMS_HOES;
    Tag<Material> SWORD = Tag.ITEMS_SWORDS;
}
