package EdDYON.guaniao.client.bath;

import EdDYON.guaniao.content.bath.BirdBathItem;
import EdDYON.guaniao.content.bath.BirdBathVariant;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/** Keep each of the six authored bath assets under GeckoLib 5's normalized IDs. */
public class BirdBathItemModel extends GeoModel<BirdBathItem> {
    private final Identifier model;
    private final Identifier texture;
    private final Identifier animation;

    public BirdBathItemModel(BirdBathVariant variant) {
        this.model = normalizedId(variant.model(), ".geo.json");
        this.texture = variant.texture();
        this.animation = normalizedId(variant.animation(), ".animation.json");
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
    public Identifier getAnimationResource(BirdBathItem animatable) {
        return this.animation;
    }

    private static Identifier normalizedId(Identifier authored, String suffix) {
        String path = authored.getPath();
        if (!path.endsWith(suffix)) {
            throw new IllegalArgumentException("Unexpected GeckoLib bath asset: " + authored);
        }
        // processResources stages byte-identical JSON under geckolib/models and
        // geckolib/animations. GeckoLib 5 adds the folder and suffix itself.
        return Identifier.fromNamespaceAndPath(authored.getNamespace(),
                path.substring(0, path.length() - suffix.length()));
    }
}
