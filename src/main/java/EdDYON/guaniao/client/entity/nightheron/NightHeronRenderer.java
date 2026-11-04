package EdDYON.guaniao.client.entity.nightheron;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Preserve the night heron's mutation scaling and beak-carried fish layer. */
public class NightHeronRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<NightHeronEntity, R> {
    public NightHeronRenderer(EntityRendererProvider.Context context) {
        super(context, new NightHeronModel());
        this.addRenderLayer(new NightHeronHeldFishLayer<>(context, this));
        this.shadowRadius = 0.21F;
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo,
                                    float widthScale, float heightScale) {
        float scale = renderPassInfo.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }
}
