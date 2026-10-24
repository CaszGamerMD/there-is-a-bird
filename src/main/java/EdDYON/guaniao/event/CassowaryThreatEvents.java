package EdDYON.guaniao.event;

import EdDYON.guaniao.content.bird.cassowary.CassowaryEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;

/**
 * Keep zombies, skeletons and spiders afraid of cassowaries.
 * The original Forge living-tick listener was gated to every twentieth tick.
 * Fabric uses a tracked set, so ordinary world ticks never need to scan every
 * loaded mob to find the ones that need the target reset.
 */
public final class CassowaryThreatEvents {
    private static final Map<ServerLevel, Map<UUID, PathfinderMob>> HOSTILES = new WeakHashMap<>();

    private CassowaryThreatEvents() { }

    public static void onEntityJoin(Entity entity, ServerLevel level) {
        if (!(entity instanceof PathfinderMob mob) || !isAffectedHostile(mob)) {
            return;
        }
        boolean alreadyInstalled = mob.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof CassowaryAvoidGoal);
        if (!alreadyInstalled) {
            mob.goalSelector.addGoal(1, new CassowaryAvoidGoal(mob));
        }
        HOSTILES.computeIfAbsent(level, ignored -> new HashMap<>()).put(mob.getUUID(), mob);
    }

    public static void onEntityLeave(Entity entity, ServerLevel level) {
        Map<UUID, PathfinderMob> hostiles = HOSTILES.get(level);
        if (hostiles != null) {
            hostiles.remove(entity.getUUID());
        }
    }

    public static void onLevelTick(ServerLevel level) {
        Map<UUID, PathfinderMob> hostiles = HOSTILES.get(level);
        if (hostiles == null) {
            return;
        }
        hostiles.values().removeIf(mob -> mob.isRemoved() || mob.level() != level);
        for (PathfinderMob mob : hostiles.values()) {
            if (Math.floorMod(mob.tickCount + mob.getId(), 20) == 0
                    && mob.getTarget() instanceof CassowaryEntity) {
                mob.setTarget(null);
            }
        }
    }

    public static void onLevelUnload(ServerLevel level) {
        HOSTILES.remove(level);
    }

    private static boolean isAffectedHostile(PathfinderMob mob) {
        return mob instanceof Zombie || mob instanceof AbstractSkeleton || mob instanceof Spider;
    }

    public static void registerCallbacks() {
        ServerEntityEvents.ENTITY_LOAD.register(CassowaryThreatEvents::onEntityJoin);
        ServerEntityEvents.ENTITY_UNLOAD.register(CassowaryThreatEvents::onEntityLeave);
        ServerTickEvents.END_LEVEL_TICK.register(CassowaryThreatEvents::onLevelTick);
        ServerLevelEvents.UNLOAD.register((server, level) -> onLevelUnload(level));
    }

    private static final class CassowaryAvoidGoal extends AvoidEntityGoal<CassowaryEntity> {
        private CassowaryAvoidGoal(PathfinderMob mob) {
            super(mob, CassowaryEntity.class, 13.0F, 1.10D, 1.35D);
        }

        @Override
        public boolean canUse() {
            boolean canUse = super.canUse();
            if (canUse && this.mob.getTarget() instanceof CassowaryEntity) {
                this.mob.setTarget(null);
            }
            return canUse;
        }
    }
}
