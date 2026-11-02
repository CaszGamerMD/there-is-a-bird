package EdDYON.guaniao.client.cage;

import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.cage.BirdCageBlockEntity;
import EdDYON.guaniao.content.cage.BirdCageVariant;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/** Select the authored size-specific cage geometry from the render snapshot. */
public class BirdCageModel extends GeoModel<BirdCageBlockEntity> {
    private static BirdCageVariant variant(GeoRenderState state) {
        BirdCageVariant variant = state.getGeckolibData(BirdCageRenderer.VARIANT);
        return variant == null ? BirdCageVariant.SMALL : variant;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(variant(renderState).model());
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return variant(renderState).texture();
    }

    @Override
    public Identifier getAnimationResource(BirdCageBlockEntity animatable) {
        return GuaniaoGeckoAssets.animation(BirdCageVariant.ANIMATION);
    }
}
