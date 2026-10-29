package EdDYON.guaniao.client.entity.nightheron;

import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.nightheron.NightHeronDefinition;
import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.model.GeoModel;

public class NightHeronModel
extends GeoModel<NightHeronEntity> {
    public Identifier getModelResource(GeoRenderState renderState) {
        return NightHeronDefinition.MODEL;
    }

    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, NightHeronDefinition.TEXTURE);
    }

    @Override
    public void addAdditionalStateData(NightHeronEntity animatable, Object relatedObject, GeoRenderState renderState) {
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(NightHeronDefinition.TEXTURE, animatable));
    }

    public Identifier getAnimationResource(NightHeronEntity animatable) {
        return NightHeronDefinition.ANIMATION;
    }
}

