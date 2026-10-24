package EdDYON.guaniao.fabric;

import net.minecraft.world.level.LevelHeightAccessor;

/** Compile-only 26.2 API proof of inclusive/exclusive build-height boundaries. */
public final class GuaniaoWorldHeightProof {
    private GuaniaoWorldHeightProof() {}

    public static boolean withinBuildableWorld(LevelHeightAccessor level, int blockY) {
        return blockY > level.getMinY() && blockY < level.getMaxY();
    }
}
