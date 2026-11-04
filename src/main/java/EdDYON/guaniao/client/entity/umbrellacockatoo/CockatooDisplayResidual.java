package EdDYON.guaniao.client.entity.umbrellacockatoo;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BoneAnimation;
import com.geckolib.cache.animation.Keyframe;
import com.geckolib.cache.animation.KeyframeStack;
import com.geckolib.loading.math.MathValue;
import com.geckolib.loading.math.value.Constant;
import com.geckolib.loading.math.value.Variable;
import java.util.Arrays;
import java.util.Set;

/**
 * Keep the original display's reference subtraction without touching GeckoLib's
 * shared baked animation. Works on the new immutable GeckoLib 5 keyframe types.
 */
final class CockatooDisplayResidual {
    static final String NAME = "animation.idle_diff_2_residual";

    static Animation create(Animation source, CockatooExpressionSampler.Pose reference) {
        BoneAnimation[] originals = source.boneAnimations();
        BoneAnimation[] result = new BoneAnimation[originals.length];
        for (int i = 0; i < result.length; i++) {
            BoneAnimation bone = originals[i];
            var delta = reference.bone(bone.boneName());
            result[i] = new BoneAnimation(bone.boneName(),
                    subtract(bone.rotationKeyFrames(), delta.rotation(), true),
                    subtract(bone.positionKeyFrames(), delta.position(), false),
                    bone.scaleKeyFrames());
        }
        return Animation.create(NAME, source.length(), source.loopType(), result, source.keyframeMarkers());
    }

    private static KeyframeStack subtract(KeyframeStack frames,
                                           CockatooExpressionSampler.Vector ref, boolean rotation) {
        return new KeyframeStack(subtract(frames.xKeyframes(), ref.x(), rotation, -1),
                subtract(frames.yKeyframes(), ref.y(), rotation, -1),
                subtract(frames.zKeyframes(), ref.z(), rotation, 1));
    }

    private static Keyframe[] subtract(Keyframe[] frames, double ref, boolean rotation, int sign) {
        if (ref == 0) return frames;
        return Arrays.stream(frames).map(frame -> new Keyframe(
                frame.startTime(), frame.length(),
                subtract(frame.startValue(), ref, rotation, sign),
                subtract(frame.endValue(), ref, rotation, sign),
                frame.easingType(), frame.easingArgs())).toArray(Keyframe[]::new);
    }

    private static MathValue subtract(MathValue value, double ref, boolean rotation, int sign) {
        // Authored constants are baked as signed radians. Molang expressions
        // use authored degree values, exactly as in the previous implementation.
        if (!value.isMutable())
            return new Constant(value.get(null) - (rotation ? Math.toRadians(ref) * sign : ref));
        return new MathValue() {
            @Override public double get(ControllerState state) {
                return value.get(state) - ref;
            }
            @Override public Set<Variable> getUsedVariables() {
                return value.getUsedVariables();
            }
        };
    }
}
