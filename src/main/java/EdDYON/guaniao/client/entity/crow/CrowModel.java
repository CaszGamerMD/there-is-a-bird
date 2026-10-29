package EdDYON.guaniao.client.entity.crow;

import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.bird.crow.CrowDefinition;
import EdDYON.guaniao.content.bird.crow.CrowEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.model.GeoModel;

public class CrowModel extends GeoModel<CrowEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(CrowDefinition.MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, CrowDefinition.TEXTURE);
    }

    @Override
    public void addAdditionalStateData(CrowEntity animatable, Object relatedObject, GeoRenderState renderState) {
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(animatable.getTextureResource(), animatable));
    }

    @Override
    public Identifier getAnimationResource(CrowEntity animatable) {
        return GuaniaoGeckoAssets.animation(CrowDefinition.ANIMATION);
    }
}
