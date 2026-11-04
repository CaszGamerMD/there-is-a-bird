package EdDYON.guaniao.client.bath;

import EdDYON.guaniao.content.bath.BirdBathBlockEntity;
import EdDYON.guaniao.content.bath.BirdBathVariant;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.google.common.reflect.TypeToken;
import net.minecraft.resources.Identifier;

/** Resolve the original six placed-bath variants through GeckoLib 5's immutable render state. */
public class BirdBathModel extends GeoModel<BirdBathBlockEntity> {
    public static final DataTicket<BirdBathVariant> VARIANT =
            DataTicket.create("guaniao_bird_bath_variant", new TypeToken<>() {});

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return BirdBathItemModel.normalizedId(variant(renderState).model(), ".geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return variant(renderState).texture();
    }

    @Override
    public Identifier getAnimationResource(BirdBathBlockEntity animatable) {
        return BirdBathItemModel.normalizedId(animatable.variant().animation(), ".animation.json");
    }

    private static BirdBathVariant variant(GeoRenderState renderState) {
        // The new block renderer will copy the actual block's variant to this
        // ticket when extracting render state; retain the default iron model for
        // a state prepared without a block entity (same as the old BE fallback).
        return renderState.getOrDefaultGeckolibData(VARIANT, BirdBathVariant.BIRD_BATH);
    }
}
