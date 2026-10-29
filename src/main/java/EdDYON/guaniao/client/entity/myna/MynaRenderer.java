package EdDYON.guaniao.client.entity.myna;

import EdDYON.guaniao.content.bird.myna.MynaEntity;
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
public class MynaRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<MynaEntity, R> {
    public MynaRenderer(EntityRendererProvider.Context context) {
        super(context, new MynaModel());
        this.shadowRadius = 0.12F;
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo,
                                    float widthScale, float heightScale) {
        float scale = renderPassInfo.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }
}
