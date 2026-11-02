package EdDYON.guaniao.proof;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Compile the 26.2 block render-state lifecycle and GeckoLib 5 per-bone
 * deferred tint submission without pulling in unported gameplay classes.
 */
public abstract class GuaniaoBirdBathBlockRendererProof<T extends BlockEntity & GeoAnimatable,
        R extends BlockEntityRenderState> extends GeoBlockRenderer<T, R> {
    private static final DataTicket<Integer> LEVEL = DataTicket.create("bath_proof_level", Integer.class);

    protected GuaniaoBirdBathBlockRendererProof(BlockEntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    public void addRenderData(T bath, Void unused, R state, float partialTick) {
        super.addRenderData(bath, unused, state, partialTick);
        state.addGeckolibData(LEVEL, 3);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> info, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(info, snapshots);
        for (GeoBone bone : info.model().topLevelBones()) {
            hide(snapshots, bone);
        }
    }

    private static void hide(BoneSnapshots snapshots, GeoBone bone) {
        snapshots.get(bone).skipRender(false);
        snapshots.get(bone).skipChildrenRender(false);
        for (GeoBone child : bone.children()) hide(snapshots, child);
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<R> info, OrderedSubmitNodeCollector tasks, RenderType renderType) {
        if (renderType == null) return;
        if (info.model().isMissingno()) {
            submitMissingModelRender(info, tasks);
            return;
        }
        int level = info.getOrDefaultGeckolibData(LEVEL, 0);
        tasks.submitCustomGeometry(info.poseStack(), renderType, (pose, consumer) -> {
            PoseStack stack = info.poseStack();
            stack.pushPose();
            stack.last().set(pose);
            info.renderPosed(() -> {
                for (GeoBone bone : info.model().topLevelBones()) {
                    renderBone(info, stack, consumer, bone, info.packedLight(), info.packedOverlay(), info.renderColor(), level);
                }
            });
            stack.popPose();
        });
    }

    private static <R extends BlockEntityRenderState> void renderBone(RenderPassInfo<R> info, PoseStack stack,
                                      VertexConsumer consumer, GeoBone bone, int light, int overlay, int color, int level) {
        stack.pushPose();
        RenderUtil.prepMatrixForBoneAndUpdateListeners(stack, bone, info);
        int tinted = (color & 0xFF000000) | (((color >>> 16) & 0xFF) * Math.min(level, 3) / 3 << 16)
                | (color & 0xFFFF);
        bone.render(info, stack, consumer, light, overlay, tinted);
        if (bone.frameSnapshot == null || !bone.frameSnapshot.areChildrenHidden()) {
            for (GeoBone child : bone.children()) {
                renderBone(info, stack, consumer, child, light, overlay, tinted, level);
            }
        }
        stack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(T entity) { return false; }

    @Override
    public int getViewDistance() { return 48; }
}
