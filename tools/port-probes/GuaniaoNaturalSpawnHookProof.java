package EdDYON.guaniao.fabric;

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

/** Isolated compilation of the 26.2 NaturalSpawner mixin callback type signature. */
@Mixin(NaturalSpawner.class)
public abstract class GuaniaoNaturalSpawnHookProof {
    @Inject(method = "mobsAt", at = @At("RETURN"), cancellable = true, require = 1)
    private static void guaniao$apiProof(ServerLevel level, StructureManager structures,
            ChunkGenerator generator, MobCategory category, BlockPos pos, Holder<Biome> biome,
            CallbackInfoReturnable<WeightedList<MobSpawnSettings.SpawnerData>> cir) {
        if (category == MobCategory.CREATURE && cir.getReturnValue() != null) {
            cir.setReturnValue(cir.getReturnValue());
        }
    }
}
