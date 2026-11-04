package EdDYON.guaniao.client.cage;

import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.cage.BirdCageItem;
import EdDYON.guaniao.content.cage.BirdCageVariant;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/** Retain each authored cage model and texture while adapting GeckoLib 5 IDs. */
public class BirdCageItemModel extends GeoModel<BirdCageItem> {
    private final Identifier model;
    private final Identifier texture;

    public BirdCageItemModel(BirdCageVariant variant) {
        this.model = GuaniaoGeckoAssets.model(variant.model());
        this.texture = variant.texture();
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return this.model;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return this.texture;
    }

    @Override
    public Identifier getAnimationResource(BirdCageItem animatable) {
        return GuaniaoGeckoAssets.animation(BirdCageVariant.ANIMATION);
    }
}
