package EdDYON.guaniao.event;

import EdDYON.guaniao.content.bird.BirdLoudSoundListener;
import EdDYON.guaniao.content.bird.BirdSleepWakeable;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Server-side response to sounds actually broadcast by ServerLevel. */
public final class BirdSleepSoundEvents {
    private static final float LOUD_SOUND_VOLUME = 1.5F;
    private static final double MIN_WAKE_RADIUS = 12.0D;
    private static final double MAX_WAKE_RADIUS = 48.0D;

    private BirdSleepSoundEvents() {
    }

    public static void onSoundAtEntity(ServerLevel level, Entity sourceEntity, float volume) {
        wakeNearbySleepingBirds(level, sourceEntity.position(), volume);
    }

    public static void onSoundAtPosition(ServerLevel level, Vec3 soundPosition, float volume) {
        wakeNearbySleepingBirds(level, soundPosition, volume);
    }

    private static void wakeNearbySleepingBirds(ServerLevel level, Vec3 soundPosition, float volume) {
        if (!Float.isFinite(volume) || volume < LOUD_SOUND_VOLUME) {
            return;
        }

        double radius = Math.min(MAX_WAKE_RADIUS, Math.max(MIN_WAKE_RADIUS, 8.0D + volume * 8.0D));
        double radiusSqr = radius * radius;
        AABB wakeArea = new AABB(soundPosition, soundPosition).inflate(radius);
        for (Entity entity : level.getEntities((Entity) null, wakeArea, candidate ->
                candidate instanceof BirdLoudSoundListener
                        || candidate instanceof BirdSleepWakeable bird && bird.isBirdSleeping())) {
            if (entity.distanceToSqr(soundPosition) <= radiusSqr) {
                if (entity instanceof BirdLoudSoundListener listener) {
                    listener.onLoudSound(soundPosition, volume);
                } else if (entity instanceof BirdSleepWakeable wakeable) {
                    wakeable.wakeFromLoudSound(soundPosition);
                }
            }
        }
    }
}
