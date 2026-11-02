package EdDYON.guaniao.client.fan;

import EdDYON.guaniao.content.fan.FeatherFanItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/**
 * The authored first-person fan charge pose, moved out of the removed Forge
 * IClientItemExtensions hook. Called by FeatherFanHandPoseMixin in Fabric's
 * 26.2 submitArmWithItem render path.
 */
public final class FeatherFanHandPose {
    private static final int MAX_CHARGE_TICKS = 30;
    private static final float CHARGE_POSE_SETTLE_TICKS = MAX_CHARGE_TICKS;

    private FeatherFanHandPose() { }

    public static boolean apply(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                ItemStack itemInHand, float partialTick, float equipProgress) {
        if (!(itemInHand.getItem() instanceof FeatherFanItem fan)) {
            return false;
        }

        InteractionHand renderedHand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (!player.isUsingItem()
                || player.getUsedItemHand() != renderedHand
                || !ItemStack.isSameItemSameComponents(player.getUseItem(), itemInHand)) {
            return false;
        }

        int usedTicks = fan.getUseDuration(itemInHand, player) - player.getUseItemRemainingTicks();
        float elapsedTicks = usedTicks + partialTick;
        float chargeProgress = Mth.clamp(elapsedTicks / MAX_CHARGE_TICKS, 0.0F, 1.0F);
        float chargeEase = smootherStep(chargeProgress);
        float progress = getDampedPoseProgress(elapsedTicks);
        float tensionEnvelope = 4.0F * chargeProgress * (1.0F - chargeProgress);
        float tensionWave = Mth.sin(elapsedTicks * 0.55F) * tensionEnvelope;
        float handDirection = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.translate(
                Mth.lerp(progress, handDirection * 0.56F, handDirection * 0.10F)
                        + handDirection * tensionWave * 0.003F,
                Mth.lerp(progress, -0.52F - equipProgress * 0.6F, -0.42F)
                        - chargeEase * 0.018F
                        + Mth.cos(elapsedTicks * 0.48F) * tensionEnvelope * 0.0015F,
                Mth.lerp(progress, -0.72F, -0.70F) + chargeEase * 0.025F
        );
        float preChargeRotation = Mth.sin(elapsedTicks * 0.45F) * tensionEnvelope * 0.6F;
        float shakeRotation = 0.0F;
        if (usedTicks >= MAX_CHARGE_TICKS) {
            float shakeTime = usedTicks - MAX_CHARGE_TICKS + partialTick;
            poseStack.translate(
                    Mth.sin(shakeTime * 2.6F) * 0.006F,
                    Mth.sin(shakeTime * 3.3F) * 0.004F,
                    0.0F
            );
            shakeRotation = Mth.sin(shakeTime * 2.9F) * 1.5F;
        }
        double modelOffsetX = handDirection * 0.9D / 16.0D;
        double modelOffsetY = 3.5D / 16.0D;
        double modelOffsetZ = 1.0D / 16.0D;
        poseStack.translate(modelOffsetX, modelOffsetY, modelOffsetZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(18.0F * progress + 7.0F * chargeEase));
        poseStack.mulPose(Axis.ZP.rotationDegrees(handDirection
                * (90.0F * progress + preChargeRotation + shakeRotation)));
        poseStack.translate(-modelOffsetX, -modelOffsetY, -modelOffsetZ);
        return true;
    }

    private static float getDampedPoseProgress(float elapsedTicks) {
        float time = Mth.clamp(elapsedTicks / CHARGE_POSE_SETTLE_TICKS, 0.0F, 1.0F);
        if (time >= 1.0F) {
            return 1.0F;
        }
        float decay = (float)Math.exp(-6.0F * time);
        float phase = 5.3F * time;
        return 1.0F - decay * (Mth.cos(phase) + 1.13F * Mth.sin(phase));
    }

    private static float smootherStep(float value) {
        value = Mth.clamp(value, 0.0F, 1.0F);
        return value * value * value * (value * (value * 6.0F - 15.0F) + 10.0F);
    }
}
