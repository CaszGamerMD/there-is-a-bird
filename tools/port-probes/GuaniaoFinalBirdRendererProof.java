import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BoneAnimation;
import com.geckolib.cache.animation.Keyframe;
import com.geckolib.cache.animation.KeyframeStack;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.loading.math.MathValue;
import com.geckolib.loading.math.value.Constant;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.RenderUtil;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Isolated Java-25 / GeckoLib-5 API compilation proof for the last two
 * migrated renderers; no legacy Forge client classes are required.
 */
public final class GuaniaoFinalBirdRendererProof {
    private GuaniaoFinalBirdRendererProof() {}

    public static <R extends LivingEntityRenderState & GeoRenderState> void tracking(
            RenderPassInfo<R> pass, BoneSnapshots snapshots) {
        snapshots.get("head").ifPresent(bone -> bone.setRotX(bone.getRotX() + 0.05f));
        pass.model().getBone("body").ifPresent(bone -> pass.addPerBoneRender(bone,
                (posed, tracked, tasks) -> {
                    Matrix4f local = RenderUtil.extractPoseFromRoot(
                            new Matrix4f(posed.poseStack().last().pose()),
                            posed.getPreRenderMatrixState());
                    Vector4f anchor = local.transform(new Vector4f(
                            -tracked.pivotX() / 16.0f,
                            (2.65f - tracked.pivotY()) / 16.0f,
                            (2.70f - tracked.pivotZ()) / 16.0f, 1f));
                    posed.renderState().getPartialTick();
                }));
    }

    public static <T extends GeoAnimatable> Animation synthesized(
            GeoModel<T> model, T bird, Animation source) {
        Animation original = model.getBakedAnimation(bird, "animation.idle");
        Keyframe neutral = new Keyframe(0, 0, new Constant(0), new Constant(0));
        KeyframeStack axis = new KeyframeStack(new Keyframe[]{neutral},
                new Keyframe[]{neutral}, new Keyframe[]{neutral});
        BoneAnimation[] bones = {new BoneAnimation("root", axis, axis, axis)};
        Animation clip = Animation.create("animation.owner_perch", source.length(),
                source.loopType(), bones, source.keyframeMarkers());
        return original == null ? clip : original;
    }

    public static DataTicket<Float> ticket() {
        return DataTicket.create("guaniao_final_bird_proof", Float.class);
    }
}
