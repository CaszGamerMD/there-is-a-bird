package EdDYON.guaniao.fabric;

import EdDYON.guaniao.GuaniaoMod;
import EdDYON.guaniao.config.BirdConfigManager;
import EdDYON.guaniao.config.BirdSpecies;
import EdDYON.guaniao.content.bird.hummingbird.FlowerHabitatIndex;
import EdDYON.guaniao.event.BirdPopulationTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;

/**
 * Exact natural-spawn restrictions formerly applied by
 * BirdSpawnConfigEvents.onSpawnPlacement in the Forge release.
 *
 * Fabric's per-type spawnPlacement callback executes before an entity is
 * created. Chunk-generation spawns are rejected before querying live-world
 * population data; regular spawn eggs, breeding and command spawns are left
 * alone exactly as the old event did.
 */
public final class GuaniaoNaturalSpawnRules {
    private static final TagKey<Biome> HUMMINGBIRD_HABITAT = TagKey.create(
            Registries.BIOME, Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "hummingbird_habitat"));

    private GuaniaoNaturalSpawnRules() { }

    public static boolean allowsPlacement(EntityType<?> entityType, ServerLevelAccessor level,
                                           EntitySpawnReason reason, BlockPos pos) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
            return true;
        }
        BirdSpecies species = BirdSpecies.from(entityType);
        if (species == null) {
            return true;
        }
        if (reason == EntitySpawnReason.CHUNK_GENERATION
                || !BirdConfigManager.allowsNaturalSpawning(species)) {
            return false;
        }
        if (species.requiresOpenSkyForNaturalSpawn() && !level.canSeeSky(pos)) {
            return false;
        }
        if (level instanceof ServerLevel server) {
            if (species == BirdSpecies.HUMMINGBIRD
                    && (!server.getBiome(pos).is(HUMMINGBIRD_HABITAT)
                    || FlowerHabitatIndex.countFlowers(server, pos, 16) < 5)) {
                return false;
            }
            if (!isBelowGlobalCaps(server, pos.getX(), pos.getZ())
                    || !isBelowSpeciesCap(server, pos.getX(), pos.getZ(), species)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBelowGlobalCaps(ServerLevel level, double x, double z) {
        int nearbyCount = BirdPopulationTracker.totalAt(level, x, z);
        int regionalCount = BirdPopulationTracker.totalInRegionAt(level, x, z);
        return BirdConfigManager.maxBirdsNearby() > 0
                && nearbyCount < BirdConfigManager.maxBirdsNearby()
                && (BirdConfigManager.maxWildBirdsPerRegion() <= 0
                    || regionalCount < BirdConfigManager.maxWildBirdsPerRegion());
    }

    private static boolean isBelowSpeciesCap(ServerLevel level, double x, double z, BirdSpecies species) {
        int limit = BirdConfigManager.maxWildNearby(species);
        return limit > 0 && BirdPopulationTracker.speciesAt(level, x, z, species) < limit;
    }
}
