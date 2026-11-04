package EdDYON.guaniao.client.entity.kingfisher;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.kingfisher.KingfisherEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/** Preserve flight pose, bill motion, carried fish and the animated dropping origin. */
public final class KingfisherRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<KingfisherEntity, R> {
    // Rest-pose soles after exported leg, foot and cube rotations.
    private static final float FOOT_OFFSET_PIXELS = -0.006486F;
    private static final DataTicket<Float> FLIGHT_PITCH =
            DataTicket.create("guaniao_kingfisher_flight_pitch", Float.class);
    private static final DataTicket<Boolean> SLEEPING =
            DataTicket.create("guaniao_kingfisher_sleeping", Boolean.class);
    private static final DataTicket<KingfisherEntity> DROPPING_SOURCE =
            DataTicket.create("guaniao_kingfisher_dropping_source", KingfisherEntity.class);
    private static final DataTicket<Vec3> DROPPING_POSITION =
            DataTicket.create("guaniao_kingfisher_dropping_position", Vec3.class);

    public KingfisherRenderer(EntityRendererProvider.Context context) {
        super(context, new KingfisherModel());
        this.shadowRadius = 0.10F;
        this.addRenderLayer(new KingfisherHeldFishLayer<>(context, this));
    }

    @Override
    public void extractRenderState(KingfisherEntity bird, R state, float partialTick) {
        super.extractRenderState(bird, state, partialTick);
        state.addGeckolibData(SLEEPING, bird.isSleeping());
        state.addGeckolibData(FLIGHT_PITCH, bird.getFlightPitch(partialTick));
        // This is a one-render-pass visual callback only; no world or handbook
        // preview ever supplies a dropping target to the deferred renderer.
        if (bird.isAlive() && bird.getActivity() == KingfisherEntity.Activity.DEFECATE
                && bird.level().isClientSide() && bird.level().getEntity(bird.getId()) == bird) {
            state.addGeckolibData(DROPPING_SOURCE, bird);
            state.addGeckolibData(DROPPING_POSITION,
                    new Vec3(Mth.lerp(partialTick, bird.xOld, bird.getX()),
                            Mth.lerp(partialTick, bird.yOld, bird.getY()),
                            Mth.lerp(partialTick, bird.zOld, bird.getZ())));
        }
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> pass, float widthScale, float heightScale) {
        float scale = pass.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(pass, widthScale * scale, heightScale * scale);
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<R> pass) {
        pass.poseStack().translate(0, FOOT_OFFSET_PIXELS / 16.0F, 0);
        super.adjustRenderPose(pass);
    }

    @Override
    protected void applyRotations(RenderPassInfo<R> pass, PoseStack pose, float nativeScale) {
        super.applyRotations(pass, pose, nativeScale);
        if (pass.renderState().deathTime > 0
                || pass.getOrDefaultGeckolibData(SLEEPING, false)) return;
        // Flight clips already extend body: only tilt the complete rig once.
        float pitch = pass.getOrDefaultGeckolibData(FLIGHT_PITCH, 0.0F);
        if (pitch != 0) {
            pose.translate(0, 3.75D / 16.0D, 0.3D / 16.0D);
            pose.mulPose(Axis.XP.rotationDegrees(-pitch));
            pose.translate(0, -3.75D / 16.0D, -0.3D / 16.0D);
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> pass, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(pass, snapshots);
        float head = pass.getOrDefaultGeckolibData(KingfisherModel.HEAD_DIP, 0.0F);
        float body = pass.getOrDefaultGeckolibData(KingfisherModel.BODY_DIP, 0.0F);
        if (head != 0) snapshots.get("head").ifPresent(bone -> bone.setRotX(bone.getRotX() + head));
        if (body != 0) snapshots.get("upbody").ifPresent(bone -> bone.setRotX(bone.getRotX() + body));
    }

    @Override
    public void preRenderPass(RenderPassInfo<R> pass, SubmitNodeCollector tasks) {
        super.preRenderPass(pass, tasks);
        if (pass.getGeckolibData(DROPPING_SOURCE) == null) return;
        // A GeckoLib 5 deferred per-bone task poses the stack to the fully
        // animated "body" bone; never mutate shared cached model bones.
        pass.model().getBone("body").ifPresent(bone -> pass.addPerBoneRender(bone,
                (posed, tracked, collector) -> {
                    KingfisherEntity bird = posed.getGeckolibData(DROPPING_SOURCE);
                    Vec3 position = posed.getGeckolibData(DROPPING_POSITION);
                    if (bird == null || position == null) return;
                    Matrix4f local = RenderUtil.extractPoseFromRoot(
                            new Matrix4f(posed.poseStack().last().pose()),
                            posed.getPreRenderMatrixState());
                    Vector4f anchor = local.transform(new Vector4f(
                            -tracked.pivotX() / 16.0F,
                            (2.65F - tracked.pivotY()) / 16.0F,
                            (2.70F - tracked.pivotZ()) / 16.0F, 1));
                    // Keep world coordinates in doubles, matching the original.
                    bird.setDroppingRenderOrigin(position.add(anchor.x(), anchor.y(), anchor.z()),
                            posed.renderState().getPartialTick());
                }));
    }
}
