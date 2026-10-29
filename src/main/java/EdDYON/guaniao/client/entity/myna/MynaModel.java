package EdDYON.guaniao.client.entity.myna;

import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.myna.MynaDefinition;
import EdDYON.guaniao.content.bird.myna.MynaEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.model.GeoModel;

public class MynaModel extends GeoModel<MynaEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MynaDefinition.MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, MynaDefinition.TEXTURE);
    }

    @Override
    public void addAdditionalStateData(MynaEntity animatable, Object relatedObject, GeoRenderState renderState) {
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(MynaDefinition.TEXTURE, animatable));
    }

    @Override
    public Identifier getAnimationResource(MynaEntity animatable) {
        return MynaDefinition.ANIMATION;
    }
}
