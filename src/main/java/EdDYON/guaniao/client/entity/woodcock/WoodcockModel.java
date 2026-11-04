package EdDYON.guaniao.client.entity.woodcock;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.bird.woodcock.WoodcockDefinition;
import EdDYON.guaniao.content.bird.woodcock.WoodcockEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/** Snapshot individual woodcock scale and the active walking/flying wing rig. */
public class WoodcockModel extends GeoModel<WoodcockEntity> {
    public static final DataTicket<Boolean> FLYING =
            DataTicket.create("guaniao_woodcock_flying", Boolean.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(WoodcockDefinition.MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, WoodcockDefinition.TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(WoodcockEntity animatable) {
        return GuaniaoGeckoAssets.animation(WoodcockDefinition.ANIMATION);
    }

    @Override
    public void addAdditionalStateData(WoodcockEntity animatable, Object relatedObject,
                                       GeoRenderState renderState) {
        renderState.addGeckolibData(BirdMutationRenderData.MODEL_SCALE,
                animatable.getModelRenderScale());
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(WoodcockDefinition.TEXTURE, animatable));
        renderState.addGeckolibData(FLYING, animatable.isFlying());
    }
}
