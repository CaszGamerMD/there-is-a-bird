package EdDYON.guaniao.client.entity.columbid;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.bird.columbid.AbstractColumbidEntity;
import EdDYON.guaniao.content.bird.columbid.ColumbidDefinition;
import EdDYON.guaniao.content.bird.columbid.ColumbidVariant;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/** Shared GeckoLib 5 model for both pigeons and spotted doves. */
public class ColumbidModel<T extends AbstractColumbidEntity> extends GeoModel<T> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(ColumbidDefinition.MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, ColumbidVariant.SPOTTED_DOVE.texture());
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return GuaniaoGeckoAssets.animation(ColumbidDefinition.ANIMATION);
    }

    @Override
    public void addAdditionalStateData(T animatable, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(BirdMutationRenderData.MODEL_SCALE,
                animatable.getModelRenderScale());
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(animatable.getTextureResource(), animatable));
    }
}
