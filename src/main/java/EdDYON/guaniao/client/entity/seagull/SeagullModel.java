package EdDYON.guaniao.client.entity.seagull;

import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.bird.seagull.SeagullDefinition;
import EdDYON.guaniao.content.bird.seagull.SeagullEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.model.GeoModel;

public class SeagullModel extends GeoModel<SeagullEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(SeagullDefinition.MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, SeagullDefinition.TEXTURE);
    }

    @Override
    public void addAdditionalStateData(SeagullEntity animatable, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(BirdMutationRenderData.MODEL_SCALE,
                animatable.getModelRenderScale());
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(SeagullDefinition.TEXTURE, animatable));
    }

    @Override
    public Identifier getAnimationResource(SeagullEntity animatable) {
        return GuaniaoGeckoAssets.animation(SeagullDefinition.ANIMATION);
    }
}
