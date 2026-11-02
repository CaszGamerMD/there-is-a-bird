package EdDYON.guaniao.proof;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.google.common.reflect.TypeToken;
import net.minecraft.resources.Identifier;

/** Compile 26.2 GeckoLib block model state tickets and suffix-less asset IDs. */
public final class GuaniaoBirdBathBlockModelProof extends GeoModel<GuaniaoBirdBathGeoItemProof> {
    private static final DataTicket<String> VARIANT = DataTicket.create("guaniao_bath_model_proof", new TypeToken<>() {});

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        String path = state.getOrDefaultGeckolibData(VARIANT, "geo/wooden_bird_bath");
        return Identifier.fromNamespaceAndPath("guaniao", path);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.fromNamespaceAndPath("guaniao", "textures/block/wooden_bird_bath.png");
    }

    @Override
    public Identifier getAnimationResource(GuaniaoBirdBathGeoItemProof animatable) {
        return Identifier.fromNamespaceAndPath("guaniao", "animations/bird_bath");
    }
}
