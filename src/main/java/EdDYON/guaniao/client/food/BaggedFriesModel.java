package EdDYON.guaniao.client.food;

import EdDYON.guaniao.content.food.BaggedFriesBlockEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;

public final class BaggedFriesModel extends GeoModel<BaggedFriesBlockEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("guaniao", "geo/bagged_fries.geo.json");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("guaniao", "textures/block/bagged_fries.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath("guaniao", "animations/bagged_fries.animation.json");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(BaggedFriesBlockEntity animatable) {
        return GuaniaoGeckoAssets.animation(ANIMATION);
    }
}
