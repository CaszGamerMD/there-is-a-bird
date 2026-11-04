package EdDYON.guaniao.fabric;

import net.minecraft.world.entity.Entity;

/** 26.2 compile-only proof of the field formerly called Entity.hasImpulse. */
public final class GuaniaoBirdMovementSyncProof {
    private GuaniaoBirdMovementSyncProof() { }

    public static void markMovementForSync(Entity entity) {
        entity.needsSync = true;
    }
}
