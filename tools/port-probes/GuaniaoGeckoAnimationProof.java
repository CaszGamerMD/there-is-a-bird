package EdDYON.guaniao.fabric;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;

/** Compile-only check of GeckoLib 5 bird animation callback API. */
public final class GuaniaoGeckoAnimationProof {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.idle");

    private GuaniaoGeckoAnimationProof() { }

    public static <T extends GeoAnimatable> AnimationController<T> controller() {
        return new AnimationController<>("movement", 4, GuaniaoGeckoAnimationProof::animate);
    }

    public static <T extends GeoAnimatable> PlayState animate(AnimationTest<T> state) {
        AnimationController<T> controller = state.controller();
        controller.setTransitionTicks(4);
        controller.setAnimationSpeed(1.0D);
        if (state.isMoving()) {
            return state.setAndContinue(IDLE);
        }
        return PlayState.STOP;
    }
}
