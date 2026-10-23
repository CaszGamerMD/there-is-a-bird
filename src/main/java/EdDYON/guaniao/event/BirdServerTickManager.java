package EdDYON.guaniao.event;

import EdDYON.guaniao.config.BirdSpecies;
import EdDYON.guaniao.content.bird.BirdGroundLocomotion;
import EdDYON.guaniao.content.bird.flight.BirdFlightLock;
import EdDYON.guaniao.content.dropping.BirdDroppingPrankHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;

/** Ticks only registered birds and affected villagers instead of every LivingEntity. */
public final class BirdServerTickManager {
    private static final Map<ServerLevel, LinkedHashMap<UUID, Mob>> BIRDS = new WeakHashMap<>();

    private BirdServerTickManager() {
    }

    public static void trackEntity(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if (entity instanceof Mob mob && BirdSpecies.from(mob) != null) {
            BIRDS.computeIfAbsent(level, ignored -> new LinkedHashMap<>()).put(mob.getUUID(), mob);
            BirdPopulationTracker.trackBird(level, mob);
        } else if (entity instanceof Villager villager) {
            BirdDroppingPrankHandler.trackVillager(villager);
        }
    }

    public static void onEntityLeave(Entity entity, ServerLevel level) {
        Map<UUID, Mob> birds = BIRDS.get(level);
        if (birds != null) {
            birds.remove(entity.getUUID());
        }
    }

    public static void onServerTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            Map<UUID, Mob> birds = BIRDS.get(level);
            if (birds == null) {
                continue;
            }
            List<Mob> birdsToDiscard = new ArrayList<>();
            Iterator<Mob> iterator = birds.values().iterator();
            while (iterator.hasNext()) {
                Mob bird = iterator.next();
                BirdSpecies species = BirdSpecies.from(bird);
                if (bird.isRemoved() || bird.level() != level || species == null) {
                    iterator.remove();
                    continue;
                }
                // Tracked border chunks can keep an entity visible while its own ticks are paused.
                if (!level.isPositionEntityTicking(bird.blockPosition())) {
                    continue;
                }
                BirdGroundLocomotion.tickBird(bird, species);
                BirdDroppingEvents.tickBird(bird);
                BirdMoltEvents.tickBird(bird);
                BirdFlightLock.tickBird(bird);
                if (BirdPopulationTracker.tickBird(bird)) {
                    birdsToDiscard.add(bird);
                }
            }
            for (Mob bird : birdsToDiscard) {
                bird.discard();
            }
        }
        BirdDroppingPrankHandler.tickTrackedVillagers(server);
    }

    public static void onLevelUnload(ServerLevel level) {
        BIRDS.remove(level);
        BirdDroppingPrankHandler.forgetLevel(level);
    }

    /** Preserve Forge END-tick cadence and entity/world cleanup using Fabric hooks. */
    public static void registerCallbacks() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> trackEntity(entity));
        ServerEntityEvents.ENTITY_UNLOAD.register(BirdServerTickManager::onEntityLeave);
        ServerTickEvents.END_SERVER_TICK.register(BirdServerTickManager::onServerTick);
        ServerLevelEvents.UNLOAD.register((server, level) -> onLevelUnload(level));
    }
}
