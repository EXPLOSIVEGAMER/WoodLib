package at.woodexplosive.woodlib.api.uninode;

import at.woodexplosive.woodlib.api.utils.BlockPos;
import at.woodexplosive.woodlib.api.utils.DirPos;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public interface INetworkProvider<T extends NodeNet<?, ?, ?>> {
    T provideNetwork();
    GenNode<T> provideNode(BlockPos... positions);

    /**
     * Visualizes a subscription attempt at a port, like HBM's {@code particleDebug}. Called by endpoints
     * from their {@code trySubscribe} while {@link UniNodespace#isParticleDebug()} is on.
     *
     * <p>By default a particle drifts along the port's axis - out of a provider into the conductor, or
     * out of the conductor into a receiver: slow sparks if the endpoint found a network, fast smoke if
     * not. Override to change the look.</p>
     *
     * @param world the world
     * @param pos the conductor in front of the port, its direction pointing away from the endpoint
     * @param receive {@code true} for a receiver, {@code false} for a provider
     * @param connected whether the endpoint subscribed to a network
     */
    default void spawnDebugParticles(@NotNull World world, @NotNull DirPos pos, boolean receive, boolean connected) {
        BlockFace dir = pos.getDir();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        // Provider: start at the port/conductor boundary, move into the conductor.
        // Receiver: start at the conductor's far side, move back into the receiver.
        double start = receive ? 0.5 : -0.5;
        double move = receive ? -1.5 : 1.5;

        double x = pos.getX() + 0.5 + dir.getModX() * start + random.nextDouble() * 0.5 - 0.25;
        double y = pos.getY() + 0.5 + dir.getModY() * start + random.nextDouble() * 0.5 - 0.25;
        double z = pos.getZ() + 0.5 + dir.getModZ() * start + random.nextDouble() * 0.5 - 0.25;

        // count 0 = a single particle moving along (dx, dy, dz) at the given speed
        world.spawnParticle(connected ? Particle.ELECTRIC_SPARK : Particle.SMOKE, x, y, z, 0,
                dir.getModX() * move, dir.getModY() * move, dir.getModZ() * move, connected ? 0.025 : 0.1);
    }
}
