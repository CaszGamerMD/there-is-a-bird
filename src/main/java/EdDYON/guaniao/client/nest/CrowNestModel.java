package EdDYON.guaniao.client.nest;

import EdDYON.guaniao.content.nest.CrowNestBlockEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class CrowNestModel extends GeoModel<CrowNestBlockEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("guaniao", "geo/crow_nest.geo.json");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("guaniao", "textures/block/crow_nest.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath("guaniao", "animations/crow_nest.animation.json");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(CrowNestBlockEntity animatable) {
        return ANIMATION;
    }
}
