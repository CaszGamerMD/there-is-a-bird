package EdDYON.guaniao.fabric;

import EdDYON.guaniao.content.bird.BirdEntityAttachments;
import net.minecraft.world.entity.Mob;

/** Compile-only proof of all five typed Fabric 26.2 entity attachment accessors. */
public final class GuaniaoBirdEntityAttachmentProof {
    private GuaniaoBirdEntityAttachmentProof() {
    }

    public static void verify(Mob bird, long tick) {
        Integer molt = bird.getAttached(BirdEntityAttachments.MOLT_COOLDOWN);
        Integer dropping = bird.getAttached(BirdEntityAttachments.DROPPING_COOLDOWN);
        Float stride = bird.getAttached(BirdEntityAttachments.INDIVIDUAL_WALK_FACTOR);
        Long lastNear = bird.getAttached(BirdEntityAttachments.LAST_NEARBY_PLAYER_TIME);
        Long flyby = bird.getAttached(BirdEntityAttachments.TRANSIENT_FLYBY_SPAWN_TIME);
        if (molt == null || dropping == null || stride == null || lastNear == null || flyby == null) {
            bird.setAttached(BirdEntityAttachments.MOLT_COOLDOWN, 12000);
            bird.setAttached(BirdEntityAttachments.DROPPING_COOLDOWN, 6000);
            bird.setAttached(BirdEntityAttachments.INDIVIDUAL_WALK_FACTOR, 1.0F);
            bird.setAttached(BirdEntityAttachments.LAST_NEARBY_PLAYER_TIME, tick);
            bird.setAttached(BirdEntityAttachments.TRANSIENT_FLYBY_SPAWN_TIME, tick);
            bird.removeAttached(BirdEntityAttachments.LAST_NEARBY_PLAYER_TIME);
        }
    }
}
