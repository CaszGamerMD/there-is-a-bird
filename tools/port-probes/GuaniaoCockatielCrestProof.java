package EdDYON.guaniao.fabric;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

/** Real GeckoLib 5 API compile contract for animated bone-relative crest offsets. */
public final class GuaniaoCockatielCrestProof {
    private static final DataTicket<Float> CREST_ROTATION =
            DataTicket.create("guaniao_cockatiel_crest_proof", Float.class);

    private GuaniaoCockatielCrestProof() { }

    public static <R extends GeoRenderState> void apply(RenderPassInfo<R> pass, BoneSnapshots snapshots) {
        float tilt = pass.getOrDefaultGeckolibData(CREST_ROTATION, 0.0F);
        if (tilt != 0.0F) {
            snapshots.get("crest").ifPresent(bone ->
                    bone.setRotX(bone.getRotX() + tilt));
        }
    }
}
