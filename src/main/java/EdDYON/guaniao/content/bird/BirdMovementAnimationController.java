package EdDYON.guaniao.content.bird;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.object.PlayState;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;

/**
 * Progress grounded bird walk clips by actual horizontal foot travel.
 *
 * GeckoLib 5 moved animation evaluation to a render-state timeline. Sampling
 * motion happens before the controller's predicate is evaluated, while the
 * timeline only consumes distance AFTER the predicate opts into ground motion.
 * This preserves the original source's stop-on-obstacle, teleport rejection,
 * scaled-stride and maximum hop-cadence rules.
 *
 * Flight, idle and transition portions use GeckoLib's normal time progression.
 * Dedicated client regression testing is still required before release.
 */
public class BirdMovementAnimationController<T extends GeoAnimatable> extends AnimationController<T> {
    private final BirdWalkDistance distance = new BirdWalkDistance();
    private double groundPixelsPerTick;
    private double maximumGroundPixelsPerTick = Double.POSITIVE_INFINITY;
    private double lastAppliedDistancePixels;

    public BirdMovementAnimationController(T bird, String name, int transitionTicks, AnimationStateHandler<T> handler) {
        // GeckoLib 5 no longer binds controller creation to an animatable instance.
        // Keep the original constructor for unchanged bird entity call sites.
        super(name, transitionTicks, handler);
    }

    void useGroundMovement(double pixelsPerTick) {
        useGroundMovement(pixelsPerTick, Double.POSITIVE_INFINITY);
    }

    void useGroundMovement(double pixelsPerTick, double maximumAnimationTicksPerTick) {
        this.groundPixelsPerTick = pixelsPerTick;
        this.maximumGroundPixelsPerTick = pixelsPerTick * maximumAnimationTicksPerTick;
        // Ground cadence comes from distance, not a permanent speed multiplier.
        setAnimationSpeed(1);
    }

    @Override
    protected boolean checkControllerState(T animatable, GeoRenderState renderState,
                                           AnimatableManager<T> manager, GeoModel<T> model) {
        // The handler re-enables distance progression only for grounded walk clips.
        this.groundPixelsPerTick = 0;

        if (animatable instanceof PathfinderMob bird) {
            float partial = renderState.getPartialTick();
            sampleTravel(renderState.getAnimatableAge(),
                    Mth.lerp(partial, bird.xo, bird.getX()),
                    Mth.lerp(partial, bird.zo, bird.getZ()),
                    BirdGroundAnimation.canPlayWalk(bird),
                    BirdGroundAnimation.renderScale(bird));
        }

        return super.checkControllerState(animatable, renderState, manager, model);
    }

    protected final void sampleTravel(double time, double x, double z, boolean grounded, double scale) {
        this.distance.sample(time, x, z, grounded, scale, this.maximumGroundPixelsPerTick);
    }

    @Override
    protected void progressExistingAnimation(T animatable, GeoRenderState renderState,
                                             double previousTime, double timeAdvanced) {
        double walkedPixels = Math.max(0, this.distance.pixels() - this.lastAppliedDistancePixels);
        this.lastAppliedDistancePixels = this.distance.pixels();

        if (this.groundPixelsPerTick > 0 && this.playState == PlayState.CONTINUE
                && this.transitionFromPoint == null && previousTime >= 0 && this.timelineTime >= 0) {
            // GeckoLib 5's animation timeline measures seconds. Walking stride
            // values remain in original model pixels per *game tick*.
            double distanceSeconds = walkedPixels / (this.groundPixelsPerTick * 20.0D);
            if (Double.isFinite(distanceSeconds)) {
                this.timelineTime = previousTime + distanceSeconds;
                super.progressExistingAnimation(animatable, renderState, previousTime, distanceSeconds);
                return;
            }
        }
        // Preserve normal real-time controller blends, flight and idle clips.
        super.progressExistingAnimation(animatable, renderState, previousTime, timeAdvanced);
    }
}
