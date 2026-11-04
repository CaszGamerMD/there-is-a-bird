package EdDYON.guaniao.client.entity.kestrel;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.kestrel.KestrelEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Original owner-head perch, airborne banking, talon pose and fetch-training name.
 * No live entity is read during the deferred GeckoLib 5 render pass.
 */
public final class KestrelRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<KestrelEntity, R> {
    private static final DataTicket<Vec3> OWNER_RENDER_OFFSET =
            DataTicket.create("guaniao_kestrel_owner_head_offset", Vec3.class);
    private static final DataTicket<Boolean> OWNER_PERCHED =
            DataTicket.create("guaniao_kestrel_owner_perched", Boolean.class);
    private static final DataTicket<Boolean> FLIGHT_ACTIVE =
            DataTicket.create("guaniao_kestrel_flight_active", Boolean.class);
    private static final DataTicket<Float> PITCH =
            DataTicket.create("guaniao_kestrel_render_pitch", Float.class);
    private static final DataTicket<Float> BANK =
            DataTicket.create("guaniao_kestrel_render_bank", Float.class);

    public KestrelRenderer(EntityRendererProvider.Context context) {
        super(context, new KestrelModel());
        this.addRenderLayer(new KestrelHeldItemLayer<>(context, this));
        this.shadowRadius = 0.18F;
    }

    @Override
    public boolean shouldShowName(KestrelEntity kestrel, double distanceToCameraSq) {
        return kestrel.isLearningFetchItem() || super.shouldShowName(kestrel, distanceToCameraSq);
    }

    @Override
    public void extractRenderState(KestrelEntity bird, R renderState, float partialTick) {
        super.extractRenderState(bird, renderState, partialTick);
        if (bird.getVehicle() instanceof AbstractClientPlayer owner && bird.isOwnedBy(owner)) {
            Vec3 ownerPosition = new Vec3(Mth.lerp(partialTick, owner.xOld, owner.getX()),
                    Mth.lerp(partialTick, owner.yOld, owner.getY()),
                    Mth.lerp(partialTick, owner.zOld, owner.getZ()));
            Vec3 birdPosition = new Vec3(Mth.lerp(partialTick, bird.xOld, bird.getX()),
                    Mth.lerp(partialTick, bird.yOld, bird.getY()),
                    Mth.lerp(partialTick, bird.zOld, bird.getZ()));
            float ownerYaw = Mth.rotLerp(partialTick, owner.yHeadRotO, owner.yHeadRot);
            float ownerPitch = Mth.lerp(partialTick, owner.xRotO, owner.getXRot());
            renderState.addGeckolibData(OWNER_RENDER_OFFSET,
                    ownerPosition.add(bird.ownerHeadOffset(ownerYaw, ownerPitch))
                            .subtract(birdPosition));
            renderState.addGeckolibData(OWNER_PERCHED, true);
            renderState.addGeckolibData(PITCH, ownerPitch);
            // In the original renderer the owner head yaw replaces the bird's body yaw.
            renderState.addGeckolibData(DataTickets.ENTITY_BODY_YAW, ownerYaw);
        } else if (bird.isFlying()) {
            renderState.addGeckolibData(FLIGHT_ACTIVE, true);
            renderState.addGeckolibData(PITCH,
                    Mth.lerp(partialTick, bird.xRotO, bird.getXRot()));
            renderState.addGeckolibData(BANK, bird.flightBank(partialTick));
        }
    }

    @Override
    public Vec3 getRenderOffset(R renderState) {
        Vec3 ownerOffset = renderState.getGeckolibData(OWNER_RENDER_OFFSET);
        return ownerOffset != null ? ownerOffset : super.getRenderOffset(renderState);
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo,
                                    float widthScale, float heightScale) {
        float scale = renderPassInfo.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }

    @Override
    protected void applyRotations(RenderPassInfo<R> renderPassInfo,
                                   PoseStack poseStack, float nativeScale) {
        super.applyRotations(renderPassInfo, poseStack, nativeScale);
        if (renderPassInfo.getOrDefaultGeckolibData(OWNER_PERCHED, false)) {
            poseStack.mulPose(Axis.XP.rotationDegrees(
                    -renderPassInfo.getOrDefaultGeckolibData(PITCH, 0.0F)));
        } else if (renderPassInfo.getOrDefaultGeckolibData(FLIGHT_ACTIVE, false)) {
            poseStack.mulPose(Axis.XP.rotationDegrees(
                    -renderPassInfo.getOrDefaultGeckolibData(PITCH, 0.0F)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(
                    renderPassInfo.getOrDefaultGeckolibData(BANK, 0.0F)));
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        if (!renderPassInfo.getOrDefaultGeckolibData(KestrelModel.EXTENDED_TALONS, false)) return;
        // Original setCustomAnimations: contact legs override flight retraction, while
        // the shared root's animation is reset for the carried prey and perching.
        for (String name : new String[]{"leg_left", "leg_right"}) {
            snapshots.get(name).ifPresent(bone -> {
                bone.setRotX(0);
                bone.setTranslateY(0);
            });
        }
        snapshots.get("root").ifPresent(bone -> {
            bone.setTranslateX(0);
            bone.setTranslateY(0);
            bone.setTranslateZ(0);
            bone.setRotX(0);
            bone.setScaleY(1);
        });
    }
}
