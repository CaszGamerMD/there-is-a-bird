package EdDYON.guaniao.client.entity.cockatoo;

import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.bird.cockatoo.CockatooDefinition;
import EdDYON.guaniao.content.bird.cockatoo.CockatooEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class CockatooModel extends GeoModel<CockatooEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(CockatooDefinition.MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, CockatooDefinition.TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(CockatooEntity animatable) {
        return GuaniaoGeckoAssets.animation(CockatooDefinition.ANIMATION);
    }

    @Override
    public void addAdditionalStateData(CockatooEntity animatable, Object relatedObject,
                                       GeoRenderState renderState) {
        renderState.addGeckolibData(BirdMutationRenderData.MODEL_SCALE,
                animatable.getModelRenderScale());
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(animatable.getTextureResource(), animatable));
    }
}
