package EdDYON.guaniao.fabric;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;

/** Compiles against real 26.2 WeightedList and record-valued SpawnerData. */
public final class GuaniaoWeightedSpawnProof {
    private GuaniaoWeightedSpawnProof() { }

    public static WeightedList<MobSpawnSettings.SpawnerData> adjust(
            WeightedList<MobSpawnSettings.SpawnerData> original, int weight) {
        List<Weighted<MobSpawnSettings.SpawnerData>> result = new ArrayList<>();
        for (Weighted<MobSpawnSettings.SpawnerData> entry : original.unwrap()) {
            var data = entry.value();
            result.add(new Weighted<>(
                    new MobSpawnSettings.SpawnerData(data.type(), data.minCount(), data.maxCount()),
                    Math.max(1, entry.weight() * weight)));
        }
        return WeightedList.of(result);
    }

    public static boolean canDraw(RandomSource random) {
        var source = WeightedList.of(List.of(
                new Weighted<>(new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 1, 3), 7)));
        return adjust(source, 2).getRandom(random).isPresent();
    }
}
