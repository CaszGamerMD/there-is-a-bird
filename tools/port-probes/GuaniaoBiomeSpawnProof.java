import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;

/** Compiled against the actual Fabric 26.2 biome-spawn registration API. */
public final class GuaniaoBiomeSpawnProof {
    private GuaniaoBiomeSpawnProof() { }

    static void verify(TagKey<Biome> habitat, EntityType<?> bird) {
        BiomeModifications.addSpawn(BiomeSelectors.tag(habitat), MobCategory.CREATURE, bird, 3, 1, 2);
    }
}
