package EdDYON.guaniao.fabric;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.gamerules.GameRules;

/** Compile-only proof of 26.2 spawn reasons, mob initialization and gamerules. */
public final class GuaniaoFlybySpawnProof {
    private GuaniaoFlybySpawnProof() { }

    public static boolean isManualKingfisherSpawn(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.SPAWN_ITEM_USE
                || reason == EntitySpawnReason.COMMAND;
    }

    public static boolean allowNaturalSpawns(ServerLevel level) {
        return level.getGameRules().get(GameRules.SPAWN_MOBS);
    }

    public static void initializeFlyby(Mob bird, ServerLevel level) {
        bird.finalizeSpawn(level, level.getCurrentDifficultyAt(bird.blockPosition()),
                EntitySpawnReason.NATURAL, null);
    }
}
