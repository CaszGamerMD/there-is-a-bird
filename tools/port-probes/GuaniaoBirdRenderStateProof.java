package EdDYON.guaniao.fabric;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.resources.Identifier;

/**
 * Isolated 26.2/GeckoLib 5 compilation gate for texture and scale
 * snapshots stored per render state, not on a shared renderer instance.
 */
public final class GuaniaoBirdRenderStateProof implements GeoBlockEntity {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("guaniao", "probe_model");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("guaniao", "textures/entity/probe.png");
    private static final DataTicket<Identifier> BIRD_TEXTURE =
            DataTicket.create("guaniao_probe_mutation_texture", Identifier.class);
    private static final DataTicket<Float> BIRD_SCALE =
            DataTicket.create("guaniao_probe_model_scale", Float.class);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private GuaniaoBirdRenderStateProof() { }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { }

    public static final class RenderStateModel extends GeoModel<GuaniaoBirdRenderStateProof> {
        @Override
        public Identifier getModelResource(GeoRenderState state) { return MODEL; }

        @Override
        public Identifier getTextureResource(GeoRenderState state) {
            return state.getOrDefaultGeckolibData(BIRD_TEXTURE, TEXTURE);
        }

        @Override
        public Identifier getAnimationResource(GuaniaoBirdRenderStateProof animatable) {
            return MODEL;
        }

        @Override
        public void addAdditionalStateData(GuaniaoBirdRenderStateProof animatable,
                                           Object associated, GeoRenderState state) {
            state.addGeckolibData(BIRD_TEXTURE, TEXTURE);
            state.addGeckolibData(BIRD_SCALE, 0.7F);
        }

        public float scale(GeoRenderState state) {
            return state.getOrDefaultGeckolibData(BIRD_SCALE, 1F);
        }
    }
}
