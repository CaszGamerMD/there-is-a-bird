package EdDYON.guaniao.client.entity.budgerigar;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.budgerigar.BudgerigarEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** GeckoLib 5 model scaling and individual mutation palettes for budgerigars. */
public class BudgerigarRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<BudgerigarEntity, R> {
    public BudgerigarRenderer(EntityRendererProvider.Context context) {
        super(context, new BudgerigarModel());
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
