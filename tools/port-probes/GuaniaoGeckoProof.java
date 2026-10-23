package EdDYON.guaniao.fabric;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

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
