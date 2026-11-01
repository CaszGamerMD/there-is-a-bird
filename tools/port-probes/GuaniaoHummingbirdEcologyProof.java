import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

/** Exercises Minecraft 26.2 and Fabric lifecycle signatures alongside the real ecology code. */
public final class GuaniaoHummingbirdEcologyProof {
    private GuaniaoHummingbirdEcologyProof() { }

    static void verify(ServerLevel level, CropBlock crop, BlockPos pos, BlockState state) {
        boolean allowed = level.getGameRules().get(GameRules.MOB_GRIEFING);
        boolean growable = crop.isValidBonemealTarget(level, pos, state);
        boolean success = crop.isBonemealSuccess(level, level.random, pos, state);
        if (allowed && growable && success) crop.performBonemeal(level, level.random, pos, state);
        level.setBlocksDirty(pos, state, state);
    }

    static void verifyCallbacks() {
        ServerTickEvents.END_LEVEL_TICK.register(level -> { });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> { });
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> { });
        ServerLevelEvents.UNLOAD.register((server, level) -> { });
    }
}
