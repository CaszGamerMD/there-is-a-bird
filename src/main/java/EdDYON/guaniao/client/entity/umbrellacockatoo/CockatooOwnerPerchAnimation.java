package EdDYON.guaniao.client.entity.umbrellacockatoo;

import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BoneAnimation;
import com.geckolib.cache.animation.Keyframe;
import com.geckolib.cache.animation.KeyframeStack;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.math.value.Constant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Derive a full neutral-channel owner-perch clip from authored idle.
 * Explicit rotation/position/scale tracks close every previous flight channel.
 */
final class CockatooOwnerPerchAnimation {
    static final String NAME = "animation.owner_perch";
    private CockatooOwnerPerchAnimation() {}

    static Animation create(Animation idle, BakedGeoModel model) {
        Map<String, BoneAnimation> original = new HashMap<>();
        for (BoneAnimation bone : idle.boneAnimations())
            original.put(bone.boneName(), bone);
        ArrayList<BoneAnimation> result = new ArrayList<>();
        for (String name : model.boneLookup().get().keySet()) {
            BoneAnimation source = original.get(name);
            result.add(new BoneAnimation(name,
                    full(source == null ? null : source.rotationKeyFrames(), 0),
                    full(source == null ? null : source.positionKeyFrames(), 0),
                    full(source == null ? null : source.scaleKeyFrames(), 1)));
        }
        return Animation.create(NAME, idle.length(), idle.loopType(),
                result.toArray(new BoneAnimation[0]), idle.keyframeMarkers());
    }

    private static KeyframeStack full(KeyframeStack source, int neutral) {
        if (source == null) return neutral(neutral);
        KeyframeStack fallback = neutral(neutral);
        return new KeyframeStack(source.xKeyframes().length == 0 ? fallback.xKeyframes() : source.xKeyframes(),
                source.yKeyframes().length == 0 ? fallback.yKeyframes() : source.yKeyframes(),
                source.zKeyframes().length == 0 ? fallback.zKeyframes() : source.zKeyframes());
    }

    private static KeyframeStack neutral(int value) {
        Keyframe key = new Keyframe(0, 0, new Constant(value), new Constant(value));
        return new KeyframeStack(new Keyframe[]{key}, new Keyframe[]{key}, new Keyframe[]{key});
    }
}
