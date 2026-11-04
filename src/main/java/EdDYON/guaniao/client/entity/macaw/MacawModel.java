package EdDYON.guaniao.client.entity.macaw;

import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.bird.macaw.MacawDefinition;
import EdDYON.guaniao.content.bird.macaw.MacawEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class MacawModel extends GeoModel<MacawEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(MacawDefinition.MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, MacawDefinition.textureForVariant(0));
    }

    @Override
    public Identifier getAnimationResource(MacawEntity animatable) {
        return GuaniaoGeckoAssets.animation(MacawDefinition.ANIMATION);
    }

    @Override
    public void addAdditionalStateData(MacawEntity animatable, Object relatedObject, GeoRenderState renderState) {
        // The authored variant is selected per bird before GeckoLib's render pass.
        renderState.addGeckolibData(BirdMutationRenderData.MODEL_SCALE,
                animatable.getModelRenderScale());
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(animatable.getTextureResource(), animatable));
    }
}
