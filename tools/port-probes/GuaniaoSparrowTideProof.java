import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.gamerules.GameRules;

/** Compile-proof for the Fabric callbacks and 26.2 spawn/gamerule APIs used by sparrow tides. */
public final class GuaniaoSparrowTideProof {
    private GuaniaoSparrowTideProof() { }

    static void check(ServerLevel level, Mob sparrow) {
        boolean spawning = level.getGameRules().get(GameRules.SPAWN_MOBS);
        sparrow.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.ZERO), EntitySpawnReason.NATURAL, null);
        level.getMinY();
        level.getMaxY();
        level.getRandom().nextFloat();
    }

    static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> { });
        ServerLevelEvents.UNLOAD.register((server, level) -> { });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> { });
    }
}
