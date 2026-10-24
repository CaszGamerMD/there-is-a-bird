package EdDYON.guaniao.fabric;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/** Independent compilation proof for native 26.2 bird spawn/path enum names. */
public final class GuaniaoBirdEnumProof {
    private GuaniaoBirdEnumProof() { }
    public static boolean natural(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION;
    }
    public static PathType water() { return PathType.WATER; }
    public static boolean fish(AbstractFish fish) { return fish.isAlive(); }
    public static ChunkStatus chunkStatus() { return ChunkStatus.FULL; }
}
