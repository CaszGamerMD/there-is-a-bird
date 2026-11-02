package EdDYON.guaniao.proof;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/** Compile real 26.2 climate/sky/biome and block-update APIs without loading the unfinished GeckoLib BE. */
public final class GuaniaoBirdBathClimateProof {
    private GuaniaoBirdBathClimateProof() {}

    private static boolean testClimate(ServerLevel level, BlockPos pos) {
        RandomSource random = level.getRandom();
        boolean openToSky = level.canSeeSky(pos.above());
        boolean rainingHere = openToSky && level.isRainingAt(pos.above());
        boolean coldEnough = level.getBiome(pos).value().coldEnoughToSnow(pos, level.getSeaLevel());
        return (rainingHere || coldEnough || (openToSky && level.isBrightOutside())) && random.nextInt(4) == 0;
    }

    private static void testBlockUpdate(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            var state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}
