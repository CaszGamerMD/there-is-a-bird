package EdDYON.guaniao.client.entity.macaw;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.macaw.MacawEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Keep mutation palettes and per-macaw scale independent in GeckoLib 5. */
public class MacawRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<MacawEntity, R> {
    public MacawRenderer(EntityRendererProvider.Context context) {
        super(context, new MacawModel());
        this.shadowRadius = 0.18F;
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo,
                                    float widthScale, float heightScale) {
        float scale = renderPassInfo.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }
}
