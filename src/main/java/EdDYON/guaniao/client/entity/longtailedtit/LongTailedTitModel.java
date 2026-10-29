package EdDYON.guaniao.client.entity.longtailedtit;

import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.longtailedtit.LongTailedTitDefinition;
import EdDYON.guaniao.content.bird.longtailedtit.LongTailedTitEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.model.GeoModel;

public class LongTailedTitModel extends GeoModel<LongTailedTitEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return LongTailedTitDefinition.MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, LongTailedTitDefinition.TEXTURE);
    }

    @Override
    public void addAdditionalStateData(LongTailedTitEntity animatable, Object relatedObject, GeoRenderState renderState) {
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(LongTailedTitDefinition.TEXTURE, animatable));
    }

    @Override
    public Identifier getAnimationResource(LongTailedTitEntity animatable) {
        return LongTailedTitDefinition.ANIMATION;
    }
}
