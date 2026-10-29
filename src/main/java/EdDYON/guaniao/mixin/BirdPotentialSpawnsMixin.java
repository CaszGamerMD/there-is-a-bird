package EdDYON.guaniao.mixin;

import EdDYON.guaniao.event.BirdSpawnConfigEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Restore the original position-dependent Forge PotentialSpawns adjustment
 * for the regular vanilla natural-spawn path in Minecraft 26.2.
 *
 * Intercepts the final biome/structure weighted pool so unrelated mob entries
 * are retained. Chunk generation uses a different path; placement predicates
 * continue to reject CHUNK_GENERATION for every bird as in the source game.
 */
@Mixin(NaturalSpawner.class)
public abstract class BirdPotentialSpawnsMixin {
    @Inject(method = "mobsAt", at = @At("RETURN"), cancellable = true, require = 1)
    private static void guaniao$adjustBirdPotentialSpawns(
            ServerLevel level, StructureManager structures, ChunkGenerator generator,
            MobCategory category, BlockPos pos, Holder<Biome> biome,
            CallbackInfoReturnable<WeightedList<MobSpawnSettings.SpawnerData>> cir) {
        if (category != MobCategory.CREATURE) {
            return;
        }
        WeightedList<MobSpawnSettings.SpawnerData> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) {
            return;
        }
        cir.setReturnValue(BirdSpawnConfigEvents.adjustedPotentialSpawns(level, pos, original));
    }
}
