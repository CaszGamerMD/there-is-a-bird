package EdDYON.guaniao.client.entity.crow;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.crow.CrowEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class CrowRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<CrowEntity, R> {
    public CrowRenderer(EntityRendererProvider.Context context) {
        super(context, new CrowModel());
        this.addRenderLayer(new CrowHeldFoodLayer<>(context, this));
        this.shadowRadius = 0.17F;
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo,
                                    float widthScale, float heightScale) {
        float scale = renderPassInfo.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }
}
