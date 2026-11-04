package EdDYON.guaniao.client.entity.sparrow;

import EdDYON.guaniao.content.bird.sparrow.SparrowEntity;
import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * GeckoLib 5 render-state renderer; preserves original species scale per bird,
 * rather than retaining a mutable GeoEntityRenderer.withScale value.
 */
public class SparrowRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<SparrowEntity, R> {
    public SparrowRenderer(EntityRendererProvider.Context context) {
        super(context, new SparrowModel());
        this.shadowRadius = 0.09F;
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo,
                                    float widthScale, float heightScale) {
        float scale = renderPassInfo.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }
}
