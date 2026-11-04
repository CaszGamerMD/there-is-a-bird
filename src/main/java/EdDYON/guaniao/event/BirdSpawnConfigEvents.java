package EdDYON.guaniao.event;

import EdDYON.guaniao.GuaniaoMod;
import EdDYON.guaniao.config.BirdConfigManager;
import EdDYON.guaniao.config.BirdSpecies;
import EdDYON.guaniao.content.bird.hummingbird.GardenAttraction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;

/**
 * Position-specific spawn-list weighting formerly attached to Forge's
 * PotentialSpawns event. Fabric callers explicitly request adjusted lists.
 *
 * The natural-spawn veto is already registered through
 * GuaniaoNaturalSpawnRules; this class preserves dynamic garden/colony weights
 * for loaded-world repopulation. Vanilla NaturalSpawner integration is separate.
 */
public final class BirdSpawnConfigEvents {
    private static final int MAX_CACHED_SPAWN_ENTRIES = 512;
    private static final Map<SpawnEntryKey, Weighted<MobSpawnSettings.SpawnerData>> SPAWN_ENTRIES =
            new LinkedHashMap<>(32, 0.75F, true);
    private static final TagKey<Biome> HUMMINGBIRD_HABITAT = TagKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "hummingbird_habitat"));

    private BirdSpawnConfigEvents() { }

    /** Return a fresh 26.2 weighted list, leaving original datapack entries unmodified. */
    public static WeightedList<MobSpawnSettings.SpawnerData> adjustedPotentialSpawns(
            ServerLevel level, BlockPos pos, WeightedList<MobSpawnSettings.SpawnerData> originalEntries) {
        List<Weighted<MobSpawnSettings.SpawnerData>> entries = new ArrayList<>();
        for (Weighted<MobSpawnSettings.SpawnerData> original : originalEntries.unwrap()) {
            MobSpawnSettings.SpawnerData data = original.value();
            BirdSpecies species = BirdSpecies.from(data.type());
            if (species == null) {
                entries.add(original);
                continue;
            }
            double multiplier = BirdConfigManager.spawnMultiplier(species);
            int available = remainingCapacity(level, pos.getX(), pos.getZ(), species);
            if (multiplier <= 0.0D || available <= 0) {
                continue;
            }
            if (species == BirdSpecies.HUMMINGBIRD) {
                if (!level.getBiome(pos).is(HUMMINGBIRD_HABITAT)) continue;
                // Budgeted loaded-chunk index: an unready or empty garden does
                // not supply a candidate and this never generates chunks.
                double gardenMultiplier = GardenAttraction.hummingbirdSpawnMultiplier(level, pos);
                if (gardenMultiplier <= 0) continue;
                multiplier *= Math.min(3.0D, gardenMultiplier);
            } else {
                multiplier *= Math.max(1.0D, Math.min(1.15D,
                        GardenAttraction.songbirdMultiplier(level, pos, species)));
            }
            int weight = Math.max(1, (int)Math.round(original.weight() * multiplier));
            int minGroup = BirdConfigManager.minGroup(species);
            int maxGroup = BirdConfigManager.maxGroup(species);
            BirdColonySpawnRules.GroupSize colonyGroup = BirdColonySpawnRules.groupAt(level, pos, species);
            if (colonyGroup != null) {
                minGroup = colonyGroup.min();
                maxGroup = colonyGroup.max();
            }
            // Cache the adjusted entry while its pack is spawning; the
            // bounded 512-entry cache preserves the original reuse intent.
            entries.add(adjustedEntry(original, weight, minGroup, maxGroup));
        }
        return WeightedList.of(entries);
    }

    private static synchronized Weighted<MobSpawnSettings.SpawnerData> adjustedEntry(
            Weighted<MobSpawnSettings.SpawnerData> original, int weight, int minGroup, int maxGroup) {
        MobSpawnSettings.SpawnerData data = original.value();
        if (original.weight() == weight && data.minCount() == minGroup && data.maxCount() == maxGroup) {
            return original;
        }
        SpawnEntryKey key = new SpawnEntryKey(data, weight, minGroup, maxGroup);
        Weighted<MobSpawnSettings.SpawnerData> adjusted = SPAWN_ENTRIES.computeIfAbsent(key,
                ignored -> new Weighted<>(new MobSpawnSettings.SpawnerData(data.type(), minGroup, maxGroup), weight));
        if (SPAWN_ENTRIES.size() > MAX_CACHED_SPAWN_ENTRIES) {
            SPAWN_ENTRIES.remove(SPAWN_ENTRIES.keySet().iterator().next());
        }
        return adjusted;
    }

    private record SpawnEntryKey(MobSpawnSettings.SpawnerData original, int weight, int minGroup, int maxGroup) { }

    static int remainingCapacity(ServerLevel level, double x, double z, BirdSpecies species) {
        int nearbyLimit = BirdConfigManager.maxBirdsNearby();
        int speciesLimit = BirdConfigManager.maxWildNearby(species);
        if (nearbyLimit <= 0 || speciesLimit <= 0) {
            return 0;
        }
        int remaining = Math.min(
                nearbyLimit - BirdPopulationTracker.totalAt(level, x, z),
                speciesLimit - BirdPopulationTracker.speciesAt(level, x, z, species)
        );
        int regionalLimit = BirdConfigManager.maxWildBirdsPerRegion();
        if (regionalLimit > 0) {
            remaining = Math.min(remaining,
                    regionalLimit - BirdPopulationTracker.totalInRegionAt(level, x, z));
        }
        return Math.max(0, remaining);
    }

    /** Compatibility entry point used by flyby spawning. */
    public static int nearbyBirdCount(ServerLevel level, double x, double y, double z) {
        return BirdPopulationTracker.totalAt(level, x, z);
    }
}
