package EdDYON.guaniao.content.dropping;

import EdDYON.guaniao.config.BirdConfigManager;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.InsideBlockEffectApplier;

/** Event-driven, one-shot pressure plate pulses caused by an impacting bird dropping. */
public final class BirdDroppingPressurePlatePulse {
    private static final Map<ServerLevel, Map<Long, Long>> ACTIVE = new WeakHashMap<>();

    private BirdDroppingPressurePlatePulse() {
    }

    public static boolean tryTrigger(ServerLevel level, BlockPos pos, Entity triggeringEntity) {
        if (!BirdConfigManager.droppingPressurePlatePulseEnabled()) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BasePressurePlateBlock)) {
            return false;
        }

        long now = level.getGameTime();
        Map<Long, Long> pulses = ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>());
        long key = pos.asLong();
        long currentExpiry = pulses.getOrDefault(key, Long.MIN_VALUE);
        if (currentExpiry > now) {
            return false;
        }

        int duration = BirdConfigManager.droppingPressurePlatePulseTicks();
        pulses.put(key, now + duration);
        state.entityInside(level, pos, triggeringEntity, InsideBlockEffectApplier.NOOP, false);
        level.scheduleTick(pos, state.getBlock(), duration);
        return true;
    }

    public static boolean isActive(ServerLevel level, BlockPos pos) {
        Map<Long, Long> pulses = ACTIVE.get(level);
        if (pulses == null) {
            return false;
        }
        long key = pos.asLong();
        Long expiry = pulses.get(key);
        if (expiry == null) {
            return false;
        }
        if (level.getGameTime() < expiry) {
            return true;
        }
        pulses.remove(key);
        if (pulses.isEmpty()) {
            ACTIVE.remove(level);
        }
        return false;
    }

    /** Fabric's per-level end tick keeps the original 200-tick cleanup cadence. */
    public static void registerCallbacks() {
        ServerLevelEvents.UNLOAD.register((server, level) -> onLevelUnload(level));
        ServerTickEvents.END_LEVEL_TICK.register(BirdDroppingPressurePlatePulse::onLevelTick);
    }

    public static void onLevelUnload(ServerLevel level) {
        ACTIVE.remove(level);
    }

    public static void onLevelTick(ServerLevel level) {
        if (level.getGameTime() % 200L != 0L) {
            return;
        }
        Map<Long, Long> pulses = ACTIVE.get(level);
        if (pulses != null) {
            long now = level.getGameTime();
            pulses.values().removeIf(expiry -> expiry <= now);
            if (pulses.isEmpty()) {
                ACTIVE.remove(level);
            }
        }
    }
}
