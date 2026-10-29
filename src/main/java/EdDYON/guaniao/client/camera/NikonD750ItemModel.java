package EdDYON.guaniao.client.camera;

import EdDYON.guaniao.GuaniaoMod;
import EdDYON.guaniao.content.camera.NikonD750Item;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;

public class NikonD750ItemModel extends GeoModel<NikonD750Item> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "geo/nikon_d750.geo.json");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "textures/item/nikon_d750.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "animations/nikon_d750.animation.json");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(NikonD750Item animatable) {
        return GuaniaoGeckoAssets.animation(ANIMATION);
    }
}
