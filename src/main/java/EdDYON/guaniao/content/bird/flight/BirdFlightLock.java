package EdDYON.guaniao.content.bird.flight;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Maintains temporary, persistent flight loss without freezing ground movement. */
public final class BirdFlightLock {
    // Persist the same absolute game tick used by the original Forge data field.
    private static final AttachmentType<Long> DISABLED_UNTIL =
            AttachmentRegistry.create(Identifier.fromNamespaceAndPath("guaniao", "flight_disabled_until"),
                    builder -> builder.persistent(Codec.LONG));

    private BirdFlightLock() {
    }

    /** Force attachment registration before worlds and bird entities load. */
    public static void registerAttachments() {
        // Class initialization registers the attachment.
    }

    public static boolean isFlightDisabled(LivingEntity bird) {
        return bird.getAttachedOrElse(DISABLED_UNTIL, 0L) > bird.level().getGameTime();
    }

    public static void disableFlight(LivingEntity bird, int durationTicks) {
        long now = bird.level().getGameTime();
        long disabledUntil = now + Math.max(0, durationTicks);
        bird.setAttached(DISABLED_UNTIL, Math.max(bird.getAttachedOrElse(DISABLED_UNTIL, 0L), disabledUntil));
        keepGrounded(bird);
    }

    public static void tickBird(LivingEntity bird) {
        long disabledUntil = bird.getAttachedOrElse(DISABLED_UNTIL, 0L);
        if (disabledUntil <= 0L) {
            return;
        }
        if (bird.level().getGameTime() >= disabledUntil) {
            bird.removeAttached(DISABLED_UNTIL);
            return;
        }
        keepGrounded(bird);
    }

    private static void keepGrounded(LivingEntity bird) {
        bird.setNoGravity(false);
        if (bird.isInWater() || bird.level().getBlockState(bird.blockPosition()).is(Blocks.BUBBLE_COLUMN)) {
            return;
        }
        Vec3 movement = bird.getDeltaMovement();
        double vertical = bird.onGround() ? Math.min(movement.y, 0.0D) : Math.min(movement.y, -0.08D);
        if (vertical != movement.y) {
            bird.setDeltaMovement(movement.x, vertical, movement.z);
        }
    }
}
