package EdDYON.guaniao.fabric;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

/**
 * Compile-only verification of GeckoLib 5's render-state and animation-timeline
 * extension points before porting BirdMovementAnimationController.
 */
public final class GuaniaoGeckoTimelineProof<T extends GeoAnimatable> extends AnimationController<T> {
    public GuaniaoGeckoTimelineProof(AnimationStateHandler<T> handler) {
        super("movement", 4, handler);
    }

    @Override
    protected boolean checkControllerState(T animatable, GeoRenderState state,
                                           AnimatableManager<T> manager, GeoModel<T> model) {
        float partial = state.getPartialTick();
        double time = state.getAnimatableAge();
        return super.checkControllerState(animatable, state, manager, model);
    }

    @Override
    protected void progressExistingAnimation(T animatable, GeoRenderState state,
                                             double previousTime, double timeAdvanced) {
        if (this.transitionFromPoint == null && this.timelineTime >= 0) {
            this.timelineTime = previousTime + Math.max(0, timeAdvanced);
        }
        super.progressExistingAnimation(animatable, state, previousTime, timeAdvanced);
    }
}
