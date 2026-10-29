package EdDYON.guaniao.client.entity.kiwi;

import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.kiwi.KiwiDefinition;
import EdDYON.guaniao.content.bird.kiwi.KiwiEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.model.GeoModel;

public class KiwiModel extends GeoModel<KiwiEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return KiwiDefinition.MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, KiwiDefinition.TEXTURE);
    }

    @Override
    public void addAdditionalStateData(KiwiEntity animatable, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(BirdMutationRenderData.MODEL_SCALE,
                animatable.getModelRenderScale());
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(KiwiDefinition.TEXTURE, animatable));
    }

    @Override
    public Identifier getAnimationResource(KiwiEntity animatable) {
        return KiwiDefinition.ANIMATION;
    }
}
