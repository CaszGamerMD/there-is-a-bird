package EdDYON.guaniao.proof;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/** Verify authored bird-bath server particles, sounds and random access against 26.2. */
public final class GuaniaoBirdBathEffectsProof {
    private GuaniaoBirdBathEffectsProof() {}

    private static void verify(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.BLOCKS, 0.45F, 0.8F);
        if (level instanceof ServerLevel serverLevel) {
            RandomSource random = serverLevel.getRandom();
            if (random.nextInt(8) == 0) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D,
                        1, 0.12D, 0.04D, 0.12D, 0.002D);
            }
        }
    }
}
