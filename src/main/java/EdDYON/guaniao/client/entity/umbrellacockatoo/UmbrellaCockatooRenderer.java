package EdDYON.guaniao.client.entity.umbrellacockatoo;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.umbrellacockatoo.UmbrellaCockatooEntity;
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
 * Per-bird owner-head perch and original overlays, applied after each
 * authored GeckoLib 5 animation, without mutating shared geometry.
 */
public class UmbrellaCockatooRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<UmbrellaCockatooEntity, R> {
    private static final DataTicket<Vec3> OWNER_RENDER_OFFSET =
            DataTicket.create("guaniao_umbrella_cockatoo_owner_offset", Vec3.class);
    private static final DataTicket<Float> OWNER_PITCH =
            DataTicket.create("guaniao_umbrella_cockatoo_owner_pitch", Float.class);

    public UmbrellaCockatooRenderer(EntityRendererProvider.Context context) {
        super(context, new UmbrellaCockatooModel());
        this.shadowRadius = 0.20F;
    }

    @Override
    public void extractRenderState(UmbrellaCockatooEntity bird, R state, float partialTick) {
        super.extractRenderState(bird, state, partialTick);
        if (bird.getVehicle() instanceof AbstractClientPlayer owner && bird.isOwnedBy(owner)) {
            Vec3 ownerPosition = new Vec3(Mth.lerp(partialTick, owner.xOld, owner.getX()),
                    Mth.lerp(partialTick, owner.yOld, owner.getY()),
                    Mth.lerp(partialTick, owner.zOld, owner.getZ()));
            Vec3 birdPosition = new Vec3(Mth.lerp(partialTick, bird.xOld, bird.getX()),
                    Mth.lerp(partialTick, bird.yOld, bird.getY()),
                    Mth.lerp(partialTick, bird.zOld, bird.getZ()));
            float headYaw = Mth.rotLerp(partialTick, owner.yHeadRotO, owner.yHeadRot);
            float pitch = Mth.lerp(partialTick, owner.xRotO, owner.getXRot());
            float bodyYaw = Mth.rotLerp(partialTick, owner.yBodyRotO, owner.yBodyRot);
            state.addGeckolibData(OWNER_RENDER_OFFSET,
                    ownerPosition.add(bird.ownerPerchOffset(headYaw, pitch, bodyYaw)).subtract(birdPosition));
            state.addGeckolibData(DataTickets.ENTITY_BODY_YAW, headYaw);
            state.addGeckolibData(OWNER_PITCH, pitch);
        }
    }

    @Override
    public Vec3 getRenderOffset(R state) {
        Vec3 offset = state.getGeckolibData(OWNER_RENDER_OFFSET);
        return offset != null ? offset : super.getRenderOffset(state);
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> pass, float widthScale, float heightScale) {
        float scale = pass.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(pass, widthScale * scale, heightScale * scale);
    }

    @Override
    protected void applyRotations(RenderPassInfo<R> pass, PoseStack pose, float nativeScale) {
        super.applyRotations(pass, pose, nativeScale);
        Float pitch = pass.getGeckolibData(OWNER_PITCH);
        if (pitch != null) pose.mulPose(Axis.XP.rotationDegrees(-pitch));
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> pass, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(pass, snapshots);
        CockatooPoseComposer.RenderPose frame = pass.getGeckolibData(UmbrellaCockatooModel.EXPRESSION_POSE);
        if (frame != null) CockatooPoseComposer.apply(snapshots, frame);
    }
}
