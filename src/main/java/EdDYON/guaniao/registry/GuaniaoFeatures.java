package EdDYON.guaniao.registry;

import EdDYON.guaniao.world.CrowNestFeature;
import EdDYON.guaniao.fabric.FabricRegistryQueue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class GuaniaoFeatures {
    public static final FabricRegistryQueue<Feature<?>> FEATURES = new FabricRegistryQueue<>(BuiltInRegistries.FEATURE, "guaniao");

    public static final FabricRegistryQueue.Ref<Feature<NoneFeatureConfiguration>> CROW_NEST = FEATURES.register("crow_nest", () ->
            new CrowNestFeature(NoneFeatureConfiguration.CODEC));

    private GuaniaoFeatures() {
    }
}
