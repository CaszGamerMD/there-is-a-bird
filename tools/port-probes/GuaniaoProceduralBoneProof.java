package EdDYON.guaniao.fabric;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

/**
 * Standalone compile-only API proof for GeckoLib 5 render-pass-specific
 * rotation, bone scale and separate wing/child visibility.
 */
public final class GuaniaoProceduralBoneProof {
    private static final DataTicket<Float> YAW =
            DataTicket.create("guaniao_procedural_yaw", Float.class);
    private static final DataTicket<Boolean> FLYING =
            DataTicket.create("guaniao_procedural_flying", Boolean.class);

    private GuaniaoProceduralBoneProof() { }

    public static <R extends GeoRenderState> void apply(RenderPassInfo<R> pass, BoneSnapshots bones) {
        float yaw = pass.getOrDefaultGeckolibData(YAW, 0.0F);
        boolean flying = pass.getOrDefaultGeckolibData(FLYING, false);
        bones.get("head").ifPresent(bone -> bone.setRotY(bone.getRotY() + yaw));
        bones.get("root_shape").ifPresent(bone -> bone.setScale(0.45F, 0.45F, 0.45F));
        bones.get("wing_left").ifPresent(bone -> {
            bone.skipRender(flying);
            bone.skipChildrenRender(flying);
        });
    }
}
