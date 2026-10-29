package EdDYON.guaniao.client.entity.columbid;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.columbid.PigeonEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** GeckoLib 5 state-based rendering preserves per-entity columbid texture and scale. */
public class PigeonRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<PigeonEntity, R> {
    public PigeonRenderer(EntityRendererProvider.Context context) {
        super(context, new ColumbidModel<>());
        this.shadowRadius = 0.15F;
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo,
                                    float widthScale, float heightScale) {
        float scale = renderPassInfo.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }
}
