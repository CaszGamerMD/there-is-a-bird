package EdDYON.guaniao.client.entity.budgerigar;

import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.bird.budgerigar.BudgerigarDefinition;
import EdDYON.guaniao.content.bird.budgerigar.BudgerigarEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class BudgerigarModel extends GeoModel<BudgerigarEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(BudgerigarDefinition.MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, BudgerigarDefinition.TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(BudgerigarEntity animatable) {
        return GuaniaoGeckoAssets.animation(BudgerigarDefinition.ANIMATION);
    }

    @Override
    public void addAdditionalStateData(BudgerigarEntity animatable, Object relatedObject,
                                       GeoRenderState renderState) {
        renderState.addGeckolibData(BirdMutationRenderData.MODEL_SCALE,
                animatable.getModelRenderScale());
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(animatable.getTextureResource(), animatable));
    }
}
