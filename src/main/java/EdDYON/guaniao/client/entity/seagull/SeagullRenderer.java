package EdDYON.guaniao.client.entity.seagull;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.seagull.SeagullEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class SeagullRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<SeagullEntity, R> {
    public SeagullRenderer(EntityRendererProvider.Context context) {
        super(context, new SeagullModel());
        this.addRenderLayer(new SeagullHeldFoodLayer<>(context, this));
        this.shadowRadius = 0.20F;
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo,
                                    float widthScale, float heightScale) {
        float scale = renderPassInfo.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }
}
