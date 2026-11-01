package EdDYON.guaniao.content.bird.hummingbird;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

/** Fabric server callbacks for the shared flower index and hummingbird crop scheduler. */
public final class HummingbirdEcologyEvents {
    private HummingbirdEcologyEvents() { }

    public static void registerCallbacks() {
        ServerTickEvents.END_LEVEL_TICK.register(HummingbirdEcologyEvents::tick);
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof Mob mob && HummingbirdPollinationService.isHummingbird(mob)) {
                HummingbirdPollinationService.remove(mob);
                FlowerHabitatIndex.releaseAll(level, mob.getUUID());
            }
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) ->
                FlowerHabitatIndex.unloadChunk(level, chunk.getPos().x, chunk.getPos().z));
        ServerLevelEvents.UNLOAD.register((server, level) -> {
            FlowerHabitatIndex.forgetLevel(level);
            HummingbirdPollinationService.forgetLevel(level);
        });
    }

    private static void tick(ServerLevel level) {
        FlowerHabitatIndex.tick(level);
        HummingbirdPollinationService.tick(level);
    }
}
