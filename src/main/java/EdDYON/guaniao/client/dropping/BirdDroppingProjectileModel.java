package EdDYON.guaniao.client.dropping;

import EdDYON.guaniao.content.dropping.BirdDroppingProjectileEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class BirdDroppingProjectileModel extends GeoModel<BirdDroppingProjectileEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("guaniao", "geo/bird_dropping_projectile.geo.json");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("guaniao", "textures/entity/bird_dropping_projectile.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath("guaniao", "animations/bird_dropping.animation.json");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(BirdDroppingProjectileEntity animatable) {
        return ANIMATION;
    }
}
