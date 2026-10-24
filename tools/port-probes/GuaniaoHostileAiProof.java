package EdDYON.guaniao.fabric;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;

/** Compile-only 26.2 API check for cassowary predator avoidance logic. */
public final class GuaniaoHostileAiProof {
    private GuaniaoHostileAiProof() { }

    public static boolean isAffected(PathfinderMob mob) {
        return mob instanceof Zombie || mob instanceof AbstractSkeleton || mob instanceof Spider;
    }

    public static void checkAvoidanceGoal(PathfinderMob mob) {
        boolean exists = mob.getGoalSelector().getAvailableGoals().stream()
                .anyMatch(goal -> goal.getGoal() instanceof AvoidEntityGoal<?>);
        if (!exists) {
            mob.getGoalSelector().addGoal(1, new AvoidEntityGoal<>(mob, Pig.class, 13.0F, 1.10D, 1.35D));
        }
        if (mob.getTarget() instanceof Pig) {
            mob.setTarget(null);
        }
    }
}
