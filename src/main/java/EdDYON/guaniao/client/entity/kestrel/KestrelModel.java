package EdDYON.guaniao.client.entity.kestrel;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.bird.kestrel.KestrelDefinition;
import EdDYON.guaniao.content.bird.kestrel.KestrelEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/**
 * Capture each kestrel's visuals before GeckoLib 5 submits a render pass.
 * Extended talons must override the authored retracting-flight clips at render time.
 */
public final class KestrelModel extends GeoModel<KestrelEntity> {
    public static final DataTicket<Boolean> EXTENDED_TALONS =
            DataTicket.create("guaniao_kestrel_extended_talons", Boolean.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(KestrelDefinition.MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, KestrelDefinition.TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(KestrelEntity animatable) {
        return GuaniaoGeckoAssets.animation(KestrelDefinition.ANIMATION);
    }

    @Override
    public void addAdditionalStateData(KestrelEntity animatable, Object relatedObject,
                                       GeoRenderState renderState) {
        renderState.addGeckolibData(BirdMutationRenderData.MODEL_SCALE,
                animatable.getModelRenderScale());
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(KestrelDefinition.TEXTURE, animatable));
        renderState.addGeckolibData(EXTENDED_TALONS, animatable.hasExtendedTalons());
    }
}
