package EdDYON.guaniao.registry;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Fabric-native registration of the original eight feather/mark particles. */
public final class GuaniaoParticleTypes {
    private static final String MOD_ID = "guaniao";
    private static final Map<String, SimpleParticleType> PARTICLES = new LinkedHashMap<>();

    public static final Supplier<SimpleParticleType> KILL_FEATHER = register("kill_feather");
    public static final Supplier<SimpleParticleType> BURIAL_WIND = register("burial_wind");
    public static final Supplier<SimpleParticleType> BURIAL_CYCLONE = register("burial_cyclone");
    public static final Supplier<SimpleParticleType> RIVEN_SPLIT = register("riven_split");
    public static final Supplier<SimpleParticleType> RIVEN_STREAK = register("riven_streak");
    public static final Supplier<SimpleParticleType> HUNTING_MARK = register("hunting_mark");
    public static final Supplier<SimpleParticleType> HUNTING_STREAK = register("hunting_streak");
    public static final Supplier<SimpleParticleType> PLACEABLE_FLECK = register("placeable_fleck");

    private GuaniaoParticleTypes() { }

    private static Supplier<SimpleParticleType> register(String id) {
        SimpleParticleType particle = FabricParticleTypes.simple();
        if (PARTICLES.putIfAbsent(id, particle) != null) {
            throw new IllegalArgumentException("Duplicate particle: " + id);
        }
        return () -> particle; // Retain old .get() call sites without Forge RegistryObject
    }

    /** Invoke exactly once from Fabric's ModInitializer, before the registries freeze. */
    public static void registerAll() {
        for (var entry : PARTICLES.entrySet()) {
            Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                    Identifier.fromNamespaceAndPath(MOD_ID, entry.getKey()), entry.getValue());
        }
    }
}
