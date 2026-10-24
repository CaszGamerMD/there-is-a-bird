package EdDYON.guaniao.fabric;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/**
 * Never instantiate or package. Type-check the actual GeckoLib 5.5.5
 * animatable interfaces and cache factory against the Fabric 26.2 classpath.
 */
public final class GuaniaoGeckoProof implements GeoBlockEntity {
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    private GuaniaoGeckoProof() { }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    /** Compile-only proof of the new GeoModel render-state getter signatures. */
    public static final class StaticModelProof extends GeoModel<GuaniaoGeckoProof> {
        private static final Identifier MODEL = Identifier.fromNamespaceAndPath("guaniao", "geo/test.geo.json");

        @Override
        public Identifier getModelResource(GeoRenderState renderState) {
            return MODEL;
        }

        @Override
        public Identifier getTextureResource(GeoRenderState renderState) {
            return MODEL;
        }

        @Override
        public Identifier getAnimationResource(GuaniaoGeckoProof animatable) {
            return MODEL;
        }
    }

    public static final class BirdAnimatableProof implements GeoEntity {
        private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return animationCache;
        }
    }
}
