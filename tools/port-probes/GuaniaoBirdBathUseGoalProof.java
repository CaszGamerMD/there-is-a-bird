package EdDYON.guaniao.proof;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

/** Verify 26.2 bird bath AI hop motion and velocity-update signaling. */
public final class GuaniaoBirdBathUseGoalProof {
    private GuaniaoBirdBathUseGoalProof() {}

    private static void verify(PathfinderMob bird, Vec3 horizontal, Vec3 position) {
        if (bird.level().isClientSide()) return;
        bird.getNavigation().stop();
        bird.setDeltaMovement(horizontal.x, 0.66D, horizontal.z);
        bird.fallDistance = 0.0F;
        bird.hurtMarked = true;
        bird.setPos(position.x, position.y, position.z);
        bird.setDeltaMovement(Vec3.ZERO);
        bird.hurtMarked = true;
    }
}
