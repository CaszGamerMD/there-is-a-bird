package EdDYON.guaniao.fabric;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Compile-only Fabric 26.2 mob spawn placement proof.
 * Never register or execute this test entity in the game.
 */
public final class GuaniaoSpawnPlacementProof {
    private GuaniaoSpawnPlacementProof() { }

    public static EntityType<Pig> createTestEntity() {
        return FabricEntityType.Builder.<Pig>createMob(Pig::new, MobCategory.CREATURE,
                builder -> builder.spawnPlacement(
                        SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        (type, level, reason, pos, random) ->
                                reason != net.minecraft.world.entity.MobSpawnType.CHUNK_GENERATION))
                .sized(0.9F, 0.9F)
                .clientTrackingRange(8)
                .build(GuaniaoRegistryIds.entity("__compile_only_spawn_placement"));
    }
}
