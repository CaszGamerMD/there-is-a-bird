package EdDYON.guaniao.event;

import EdDYON.guaniao.content.bird.BirdScanBudget;
import EdDYON.guaniao.content.bird.BirdTags;
import EdDYON.guaniao.content.earthworm.EarthwormEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public final class EarthwormRainEvents {
    private static final Map<ServerLevel, Map<Long, Long>> COOLDOWNS = new WeakHashMap<>();

    private EarthwormRainEvents() { }

    public static void onLevelTick(ServerLevel level) {
        long now = level.getGameTime();
        if (now % 200L != 0L) return;
        Map<Long, Long> cells = COOLDOWNS.computeIfAbsent(level, ignored -> new HashMap<>());
        cells.values().removeIf(until -> until <= now);
        if (!level.isRaining() || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                || level.players().isEmpty() || level.random.nextFloat() >= 0.35F) return;
        ServerPlayer player = level.players().get((int) ((now / 200L) % level.players().size()));
        if (player.isSpectator() || !BirdScanBudget.tryAcquire(level, 1)) return;
        for (int attempt = 0; attempt < 8; attempt++) {
            int x = player.getBlockX() + level.random.nextInt(33) - 16;
            int z = player.getBlockZ() + level.random.nextInt(33) - 16;
            BlockPos column = new BlockPos(x, player.getBlockY(), z);
            if (!level.hasChunkAt(column)) continue;
            long cell = ChunkPos.asLong(Math.floorDiv(x, 32), Math.floorDiv(z, 32));
            boolean cooling = false;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (cells.getOrDefault(ChunkPos.asLong(Math.floorDiv(x, 32) + dx,
                            Math.floorDiv(z, 32) + dz), 0L) > now) cooling = true;
                }
            }
            if (cooling) continue;
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
            if (Math.abs(surface.getY() - player.getY()) > 12.0D || !level.isRainingAt(surface)
                    || !level.getBlockState(surface.below()).is(BirdTags.WOODCOCK_FORAGE_GROUND)) continue;
            if (EarthwormEntity.spawn(level, Vec3.atBottomCenterOf(surface).add(0.0D, 0.01D, 0.0D), true) != null) {
                cells.put(cell, now + 800L);
                return;
            }
        }
    }

    public static void onUnload(ServerLevel level) {
        COOLDOWNS.remove(level);
    }

    public static void registerCallbacks() {
        ServerTickEvents.END_LEVEL_TICK.register(EarthwormRainEvents::onLevelTick);
        ServerLevelEvents.UNLOAD.register((server, level) -> onUnload(level));
    }
}
