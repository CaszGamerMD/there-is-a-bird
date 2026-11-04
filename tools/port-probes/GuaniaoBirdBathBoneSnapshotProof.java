package EdDYON.guaniao.proof;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.renderer.base.BoneSnapshots;

/** Verify GeckoLib 5's per-frame bone flags and names for the bath contents. */
public final class GuaniaoBirdBathBoneSnapshotProof {
    private GuaniaoBirdBathBoneSnapshotProof() {}

    private static void hide(BoneSnapshots snapshots, String boneName, boolean visible) {
        snapshots.ifPresent(boneName, snapshot -> {
            BoneSnapshot bone = snapshot;
            String actualName = bone.getBone().name();
            if (actualName.equals(boneName)) {
                bone.skipRender(!visible);
                bone.skipChildrenRender(!visible);
            }
        });
    }
}
