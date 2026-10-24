package EdDYON.guaniao.portprobe;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Isolated compile-only proof of Fabric 26.2 pressure plate and server callbacks. */
public final class GuaniaoPressurePlateProof {
    private GuaniaoPressurePlateProof() { }

    public static void probe(ServerLevel level, BlockPos pos, Entity triggeringEntity) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BasePressurePlateBlock) {
            state.entityInside(level, pos, triggeringEntity, InsideBlockEffectApplier.NOOP, false);
        }
    }

    public static void registerProbe() {
        ServerLevelEvents.UNLOAD.register((server, level) -> { });
        ServerTickEvents.END_LEVEL_TICK.register(level -> { });
    }
}
