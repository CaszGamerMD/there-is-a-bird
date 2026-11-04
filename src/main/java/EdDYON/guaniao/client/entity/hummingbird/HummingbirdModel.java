package EdDYON.guaniao.client.entity.hummingbird;

import EdDYON.guaniao.content.bird.hummingbird.HummingbirdEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/** Preserve the author's tiny model and owner-head perch support pose. */
public final class HummingbirdModel extends GeoModel<HummingbirdEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("guaniao", "geo/hummingbird.geo.json");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("guaniao", "textures/entity/hummingbird.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath("guaniao", "animations/hummingbird_runtime.animation.json");
    public static final DataTicket<Boolean> HEAD_PERCHED =
            DataTicket.create("guaniao_hummingbird_owner_head_perch", Boolean.class);
    public static final String[] HEAD_PERCH_SUPPORT_BONES = {
            "root", "all", "waist", "leg_left", "leg_right", "foot_left", "foot_right"
    };

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets.model(MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(HummingbirdEntity animatable) {
        return EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets.animation(ANIMATION);
    }

    @Override
    public void addAdditionalStateData(HummingbirdEntity bird, Object relatedObject,
                                       GeoRenderState renderState) {
        renderState.addGeckolibData(HEAD_PERCHED,
                bird.getVehicle() instanceof Player owner && bird.isOwnedBy(owner));
    }
}
