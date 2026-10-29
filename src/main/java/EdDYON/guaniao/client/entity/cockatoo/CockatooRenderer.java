package EdDYON.guaniao.client.entity.cockatoo;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.cockatoo.CockatooDefinition;
import EdDYON.guaniao.content.bird.cockatoo.CockatooEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Preserve individual scale and the authored 0.751-pixel toe correction. */
public class CockatooRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<CockatooEntity, R> {
    public CockatooRenderer(EntityRendererProvider.Context context) {
        super(context, new CockatooModel());
        this.shadowRadius = 0.22F;
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo,
                                    float widthScale, float heightScale) {
        float scale = renderPassInfo.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<R> renderPassInfo) {
        // Called after GeckoLib scales the pose: local pixels automatically
        // track the bird's individual scale. Keep this before base rotations.
        renderPassInfo.poseStack().translate(0, CockatooDefinition.FOOT_OFFSET_PIXELS / 16.0F, 0);
        super.adjustRenderPose(renderPassInfo);
    }
}
