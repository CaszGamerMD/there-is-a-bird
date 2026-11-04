package EdDYON.guaniao.content.bird.flight;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;

/**
 * Play bird flight without holding a stale ground clip after takeoff.
 *
 * GeckoLib 5 renamed the animation reset/transition API. Keep the original
 * replacement-only recovery predicate so healthy multi-stage flight loops
 * retain their animation progress.
 */
public final class BirdFlightAnimation {
    private BirdFlightAnimation() { }

    public static <T extends GeoAnimatable> PlayState play(AnimationTest<T> state, RawAnimation animation) {
        AnimationController<T> controller = state.controller();
        // Ground movement handlers restore their blend duration every frame.
        controller.setTransitionTicks(0);
        if (state.isCurrentAnimation(animation)
                && (controller.getPlayState() == PlayState.STOP
                || controller.getPlayState() == PlayState.CONTINUE
                && animation.getAnimationStages().stream().noneMatch(stage ->
                        state.isCurrentAnimationStage(stage.animationName())))) {
            controller.reset();
        }
        return state.setAndContinue(animation);
    }
}
