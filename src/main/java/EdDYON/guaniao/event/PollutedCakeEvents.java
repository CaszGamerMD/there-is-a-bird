package EdDYON.guaniao.event;

import EdDYON.guaniao.content.dropping.PollutedCakeData;
import EdDYON.guaniao.content.dropping.PrankFoodEffectUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Preserve polluted cake cleanup and the prank effect when an edible polluted
 * cake is used. Always return PASS so vanilla handles the actual cake bite.
 */
public final class PollutedCakeEvents {
    private PollutedCakeEvents() { }

    public static void onLevelTick(ServerLevel level) {
        PollutedCakeData.get(level).tick(level);
    }

    public static void onChunkLoad(ServerLevel level, LevelChunk chunk) {
        PollutedCakeData.get(level).validateChunk(chunk);
    }

    public static InteractionResult onRightClickCake(Player player, Level world,
                                                      InteractionHand hand, BlockHitResult hit) {
        if (!(world instanceof ServerLevel level)
                || player.isSpectator()
                || !(level.getBlockState(hit.getBlockPos()).getBlock() instanceof CakeBlock)
                || !PollutedCakeData.get(level).isPolluted(hit.getBlockPos())
                || !player.canEat(false)) {
            return InteractionResult.PASS;
        }
        PrankFoodEffectUtil.apply(player);
        return InteractionResult.PASS;
    }

    public static void registerCallbacks() {
        ServerTickEvents.END_LEVEL_TICK.register(PollutedCakeEvents::onLevelTick);
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> onChunkLoad(level, chunk));
        UseBlockCallback.EVENT.register(PollutedCakeEvents::onRightClickCake);
    }
}
