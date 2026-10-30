package EdDYON.guaniao.client.entity.hummingbird;

import EdDYON.guaniao.content.bird.hummingbird.HummingbirdEntity;
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

/** Original owner-head perch and authored small scale are preserved. */
public final class HummingbirdRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<HummingbirdEntity, R> {
    private static final DataTicket<Vec3> OWNER_RENDER_OFFSET =
            DataTicket.create("guaniao_hummingbird_owner_head_offset", Vec3.class);
    private static final DataTicket<Float> OWNER_PITCH =
            DataTicket.create("guaniao_hummingbird_owner_pitch", Float.class);

    public HummingbirdRenderer(EntityRendererProvider.Context context) {
        super(context, new HummingbirdModel());
        this.shadowRadius = 0.06F;
        this.addRenderLayer(new HummingbirdHeldItemLayer<>(context, this));
    }

    @Override
    public void extractRenderState(HummingbirdEntity bird, R renderState, float partialTick) {
        super.extractRenderState(bird, renderState, partialTick);
        if (bird.getVehicle() instanceof AbstractClientPlayer owner && bird.isOwnedBy(owner)) {
            Vec3 ownerPosition = new Vec3(Mth.lerp(partialTick, owner.xOld, owner.getX()),
                    Mth.lerp(partialTick, owner.yOld, owner.getY()),
                    Mth.lerp(partialTick, owner.zOld, owner.getZ()));
            Vec3 birdPosition = new Vec3(Mth.lerp(partialTick, bird.xOld, bird.getX()),
                    Mth.lerp(partialTick, bird.yOld, bird.getY()),
                    Mth.lerp(partialTick, bird.zOld, bird.getZ()));
            float yaw = Mth.rotLerp(partialTick, owner.yHeadRotO, owner.yHeadRot);
            float pitch = Mth.lerp(partialTick, owner.xRotO, owner.getXRot());
            renderState.addGeckolibData(OWNER_RENDER_OFFSET,
                    ownerPosition.add(bird.ownerHeadOffset(yaw, pitch)).subtract(birdPosition));
            renderState.addGeckolibData(DataTickets.ENTITY_BODY_YAW, yaw);
            renderState.addGeckolibData(OWNER_PITCH, pitch);
        }
    }

    @Override
    public Vec3 getRenderOffset(R renderState) {
        Vec3 offset = renderState.getGeckolibData(OWNER_RENDER_OFFSET);
        return offset != null ? offset : super.getRenderOffset(renderState);
    }

    @Override
    protected void applyRotations(RenderPassInfo<R> renderPassInfo,
                                  PoseStack poseStack, float nativeScale) {
        super.applyRotations(renderPassInfo, poseStack, nativeScale);
        Float pitch = renderPassInfo.getGeckolibData(OWNER_PITCH);
        if (pitch != null) poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        if (!renderPassInfo.getOrDefaultGeckolibData(HummingbirdModel.HEAD_PERCHED, false)) return;
        // The seven support bones return to their authored base/bind transform
        // during fly-to-idle blending. Other bones retain their active animation.
        for (String name : HummingbirdModel.HEAD_PERCH_SUPPORT_BONES) {
            snapshots.get(name).ifPresent(bone -> {
                bone.setRotation(0, 0, 0);
                bone.setTranslation(0, 0, 0);
                bone.setScale(1, 1, 1);
            });
        }
    }
}
