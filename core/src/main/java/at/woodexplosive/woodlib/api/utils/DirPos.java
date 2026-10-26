package at.woodexplosive.woodlib.api.utils;

import org.bukkit.block.BlockFace;

public class DirPos extends BlockPos {
    public static final BlockFace[] CARDINAL_6 = { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST };
    private final BlockFace dir;

    public DirPos(int x, int y, int z, BlockFace dir) {
        super(x, y, z);
        this.dir = dir;
    }

    public DirPos(BlockPos pos, BlockFace dir) {
        this(pos.getX(), pos.getY(), pos.getZ(), dir);
    }

    public static DirPos of(BlockPos self, BlockFace dir) {
        return new DirPos(self.offset(dir), dir);
    }

    public BlockFace getDir() {
        return dir;
    }
}
