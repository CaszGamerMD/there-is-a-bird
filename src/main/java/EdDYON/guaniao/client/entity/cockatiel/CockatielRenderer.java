package EdDYON.guaniao.client.entity.cockatiel;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.cockatiel.CockatielEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Apply the authored crest tilt on top of GeckoLib 5's animated bone snapshot. */
public class CockatielRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<CockatielEntity, R> {
    public CockatielRenderer(EntityRendererProvider.Context context) {
        super(context, new CockatielModel());
        this.shadowRadius = 0.12F;
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
        float offset = renderPassInfo.getOrDefaultGeckolibData(CockatielModel.CREST_ROTATION, 0.0F);
        if (offset != 0.0F) {
            snapshots.get("crest").ifPresent(crest ->
                    crest.setRotX(crest.getRotX() + offset));
        }
    }
}
