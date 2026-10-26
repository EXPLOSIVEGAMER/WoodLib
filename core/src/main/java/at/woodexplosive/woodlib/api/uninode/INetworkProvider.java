package at.woodexplosive.woodlib.api.uninode;

import at.woodexplosive.woodlib.api.utils.BlockPos;
import at.woodexplosive.woodlib.api.utils.DirPos;
import org.bukkit.World;

public interface INetworkProvider<T extends NodeNet<?, ?, ?>> {
    T provideNetwork();
    GenNode<T> provideNode(BlockPos... positions);

    default void spawnDebugParticles(World world, DirPos pos, boolean receive) {}
}
