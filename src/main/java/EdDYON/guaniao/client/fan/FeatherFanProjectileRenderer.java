package EdDYON.guaniao.client.fan;

import EdDYON.guaniao.content.fan.FeatherFanProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Extract the authored spinning, piercing, hunting and Riven fan poses before
 * deferred item submission. Every Riven copy retains its original model seed.
 */
public class FeatherFanProjectileRenderer
        extends EntityRenderer<FeatherFanProjectileEntity, FeatherFanProjectileRenderState> {
    private static final float RENDER_SCALE = 1.35F;
    private final ItemModelResolver itemModelResolver;

    public FeatherFanProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.shadowRadius = 0.0F;
    }

    @Override
    public FeatherFanProjectileRenderState createRenderState() {
        return new FeatherFanProjectileRenderState();
    }

    @Override
    public void extractRenderState(FeatherFanProjectileEntity entity,
                                   FeatherFanProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ticks = entity.tickCount;
        state.partialTick = partialTick;
        state.yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        state.pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        state.charge = entity.getCharge();
        state.rivenTicks = entity.getRivenTicks();
        state.piercing = entity.isPiercing();
        state.stuck = entity.isStuck();
        state.hunting = entity.isHunting();
        state.returning = entity.isReturning();
        state.rivenActive = entity.isRivenActive();
        // Old immediate rendering resolved the GROUND item once per copy,
        // using entity.getId() for the main fan and id*31+i for Riven.
        this.itemModelResolver.updateForTopItem(state.mainFan, entity.getItem(),
                ItemDisplayContext.GROUND, entity.level(), null, entity.getId());
        for (int i = 0; i < state.rivenFans.length; i++) {
            this.itemModelResolver.updateForTopItem(state.rivenFans[i], entity.getItem(),
                    ItemDisplayContext.GROUND, entity.level(), null, entity.getId() * 31 + i);
        }
    }

    @Override
    public void submit(FeatherFanProjectileRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.ticks < 2) {
            super.submit(state, poseStack, collector, camera);
            return;
        }

        boolean renderMainFan = !state.rivenActive
                || state.rivenTicks < FeatherFanProjectileEntity.RIVEN_PREPARE_END
                || state.rivenTicks >= FeatherFanProjectileEntity.RIVEN_CONVERGE_END;
        if (renderMainFan) {
            this.submitMainFan(state, poseStack, collector);
        }
        if (state.rivenActive) {
            this.submitRivenArray(state, poseStack, collector);
        }
        super.submit(state, poseStack, collector, camera);
    }

    private void submitMainFan(FeatherFanProjectileRenderState state, PoseStack poseStack,
                               SubmitNodeCollector collector) {
        poseStack.pushPose();
        poseStack.scale(RENDER_SCALE, RENDER_SCALE, RENDER_SCALE);
        if (state.piercing || state.stuck) {
            // Point the fan along the flight vector without changing authored proportions.
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F - state.pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(43.0F));
            poseStack.scale(0.90F, 0.90F, 0.90F);
            if (state.rivenActive
                    && state.rivenTicks >= FeatherFanProjectileEntity.RIVEN_CONVERGE_END) {
                float reformProgress = Mth.clamp(
                        (state.rivenTicks + state.partialTick - FeatherFanProjectileEntity.RIVEN_CONVERGE_END)
                                / (FeatherFanProjectileEntity.RIVEN_END
                                - FeatherFanProjectileEntity.RIVEN_CONVERGE_END),
                        0.0F,
                        1.0F
                );
                float eased = reformProgress * reformProgress * (3.0F - 2.0F * reformProgress);
                float reformScale = Mth.lerp(eased, 0.34F, 1.0F);
                poseStack.mulPose(Axis.ZP.rotationDegrees((1.0F - eased) * 240.0F));
                poseStack.scale(reformScale, reformScale, reformScale);
            }
        } else {
            float spinSpeed = state.hunting
                    ? 72.0F
                    : Mth.lerp(state.charge, 30.0F, 55.0F);
            float spinDirection = state.returning ? -1.0F : 1.0F;
            poseStack.mulPose(Axis.YP.rotationDegrees(
                    (state.ticks + state.partialTick) * spinSpeed * spinDirection));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            if (state.hunting) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(
                        Mth.sin((state.ticks + state.partialTick) * 0.72F) * 7.5F));
                poseStack.scale(1.08F, 1.08F, 1.08F);
            }
        }
        state.mainFan.submit(poseStack, collector,
                state.hunting ? LightCoordsUtil.FULL_BRIGHT : state.lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    private void submitRivenArray(FeatherFanProjectileRenderState state,
                                  PoseStack poseStack, SubmitNodeCollector collector) {
        float age = state.rivenTicks + state.partialTick;
        float radius = FeatherFanProjectileEntity.getRivenArrayRadius(age);
        if (radius <= 0.0F) {
            return;
        }

        double ringRotation = FeatherFanProjectileEntity.getRivenRingRotation(age);
        double heightScale = radius / 3.8D;
        float pulse = 0.82F + Mth.sin(age * 0.82F) * 0.035F;
        for (int i = 0; i < 8; i++) {
            double baseAngle = Mth.TWO_PI * i / 8.0D;
            double angle = baseAngle + ringRotation;
            double x = Math.cos(angle) * radius;
            double y = Math.sin(baseAngle * 2.0D) * 0.85D * heightScale;
            double z = Math.sin(angle) * radius;
            double directionLength = Math.sqrt(x * x + y * y + z * z);
            if (directionLength < 1.0E-5D) {
                continue;
            }

            double directionX = -x / directionLength;
            double directionY = -y / directionLength;
            double directionZ = -z / directionLength;
            double horizontal = Math.sqrt(directionX * directionX + directionZ * directionZ);
            float yaw = (float)Math.toDegrees(Mth.atan2(directionX, directionZ));
            float pitch = (float)Math.toDegrees(Mth.atan2(directionY, horizontal));

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.scale(RENDER_SCALE * pulse, RENDER_SCALE * pulse, RENDER_SCALE * pulse);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F - pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(43.0F));
            poseStack.scale(0.82F, 0.82F, 0.82F);
            state.rivenFans[i].submit(poseStack, collector, LightCoordsUtil.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
