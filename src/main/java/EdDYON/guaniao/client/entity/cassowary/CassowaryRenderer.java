package EdDYON.guaniao.client.entity.cassowary;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.cassowary.CassowaryEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Add procedural head/neck gaze to animated bone snapshots per cassowary. */
public final class CassowaryRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<CassowaryEntity, R> {
    public CassowaryRenderer(EntityRendererProvider.Context context) {
        super(context, new CassowaryModel());
        this.shadowRadius = 0.493F;
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo,
                                    float widthScale, float heightScale) {
        float scale = renderPassInfo.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        addRotation(renderPassInfo, snapshots, "head", CassowaryModel.HEAD_PITCH, CassowaryModel.HEAD_YAW);
        addRotation(renderPassInfo, snapshots, "neckUpper", CassowaryModel.UPPER_NECK_PITCH,
                CassowaryModel.UPPER_NECK_YAW);
        addRotation(renderPassInfo, snapshots, "neckLower", CassowaryModel.LOWER_NECK_PITCH,
                CassowaryModel.LOWER_NECK_YAW);
    }

    private static <R extends GeoRenderState> void addRotation(RenderPassInfo<R> renderPassInfo,
            BoneSnapshots snapshots, String boneName, DataTicket<Float> pitchTicket,
            DataTicket<Float> yawTicket) {
        float pitch = renderPassInfo.getOrDefaultGeckolibData(pitchTicket, 0.0F);
        float yaw = renderPassInfo.getOrDefaultGeckolibData(yawTicket, 0.0F);
        if (pitch == 0.0F && yaw == 0.0F) return;
        snapshots.get(boneName).ifPresent(bone -> {
            bone.setRotX(bone.getRotX() + pitch);
            bone.setRotY(bone.getRotY() + yaw);
        });
    }
}
