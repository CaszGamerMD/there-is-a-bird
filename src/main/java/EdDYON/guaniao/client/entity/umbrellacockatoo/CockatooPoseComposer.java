package EdDYON.guaniao.client.entity.umbrellacockatoo;

import com.geckolib.renderer.base.BoneSnapshots;
import java.util.HashSet;
import java.util.Set;

/** Apply expression deltas to this render pass's snapshots, never shared cached bones. */
final class CockatooPoseComposer {
    record RenderPose(CockatooExpressionSampler.Pose emotion, float headRoll) {
        CockatooExpressionSampler.Delta bone(String name) {
            var delta = emotion.bone(name);
            double roll = name.equals("head") ? headRoll : name.equals("neck") ? headRoll * 0.35 : 0;
            if (roll == 0) return delta;
            return new CockatooExpressionSampler.Delta(
                    delta.rotation().add(new CockatooExpressionSampler.Vector(0, 0, roll)), delta.position());
        }
    }

    static void apply(BoneSnapshots snapshots, RenderPose pose) {
        Set<String> names = new HashSet<>(CockatooExpressionSampler.BONES);
        names.add("head");
        names.add("neck");
        for (String name : names) {
            CockatooExpressionSampler.Delta delta = pose.bone(name);
            if (delta.equals(CockatooExpressionSampler.Delta.ZERO)) continue;
            snapshots.get(name).ifPresent(bone -> {
                var r = delta.rotation();
                var p = delta.position();
                bone.setRotX(bone.getRotX() - (float)Math.toRadians(r.x()));
                bone.setRotY(bone.getRotY() - (float)Math.toRadians(r.y()));
                bone.setRotZ(bone.getRotZ() + (float)Math.toRadians(r.z()));
                bone.setTranslateX(bone.getTranslateX() + (float)p.x());
                bone.setTranslateY(bone.getTranslateY() + (float)p.y());
                bone.setTranslateZ(bone.getTranslateZ() + (float)p.z());
            });
        }
    }
}
