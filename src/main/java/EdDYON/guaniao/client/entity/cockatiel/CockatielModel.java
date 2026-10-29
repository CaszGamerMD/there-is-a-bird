package EdDYON.guaniao.client.entity.cockatiel;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.bird.cockatiel.CockatielCrestState;
import EdDYON.guaniao.content.bird.cockatiel.CockatielDefinition;
import EdDYON.guaniao.content.bird.cockatiel.CockatielEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/** Five authored cockatiel palettes and five emotion-specific crest poses. */
public class CockatielModel extends GeoModel<CockatielEntity> {
    public static final DataTicket<Float> CREST_ROTATION =
            DataTicket.create("guaniao_cockatiel_crest_rotation", Float.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(CockatielDefinition.MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, CockatielDefinition.textureForVariant(0));
    }

    @Override
    public Identifier getAnimationResource(CockatielEntity animatable) {
        return GuaniaoGeckoAssets.animation(CockatielDefinition.ANIMATION);
    }

    @Override
    public void addAdditionalStateData(CockatielEntity animatable, Object relatedObject,
                                       GeoRenderState renderState) {
        renderState.addGeckolibData(BirdMutationRenderData.MODEL_SCALE,
                animatable.getModelRenderScale());
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(animatable.getTextureResource(), animatable));
        CockatielCrestState state = animatable.getCrestState();
        float offset = switch (state) {
            case RELAXED -> 0.0F;
            case HAPPY -> -0.12F;
            case CURIOUS -> -0.22F;
            case ALERT -> -0.38F;
            case AFRAID -> -0.56F;
        };
        renderState.addGeckolibData(CREST_ROTATION, offset);
    }
}
