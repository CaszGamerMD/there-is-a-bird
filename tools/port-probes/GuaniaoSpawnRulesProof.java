package EdDYON.guaniao.fabric;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;

public final class GuaniaoSpawnRulesProof {
    private GuaniaoSpawnRulesProof() {}

    public static boolean enabled(ServerLevel level) {
        return level.getGameRules().get(GameRules.SPAWN_MOBS) &&
                level.isRaining() &&
                level.getRandom().nextFloat() < 0.35F;
    }
}
