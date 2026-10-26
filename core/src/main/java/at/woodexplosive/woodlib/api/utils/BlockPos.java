package at.woodexplosive.woodlib.api.utils;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Objects;

public class BlockPos {
    private final int x;
    private final int y;
    private final int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockPos of(Block block) {
        return new BlockPos(block.getX(), block.getY(), block.getZ());
    }

    public BlockPos offset(BlockFace face) {
        return new BlockPos(this.x + face.getModX(), this.y + face.getModY(), this.z + face.getModZ());
    }

    public Block toBlock(World world) {
        return world.getBlockAt(this.x, this.y, this.z);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockPos p && x == p.x && y == p.y && z == p.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
