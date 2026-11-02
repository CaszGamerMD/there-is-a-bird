package EdDYON.guaniao.client.bath;

import EdDYON.guaniao.content.bath.BirdBathBlockEntity;
import EdDYON.guaniao.content.bath.BirdBathCleanliness;
import EdDYON.guaniao.content.bath.BirdBathContentType;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
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

/**
 * Render each placed bath from a 26.2 snapshot, retaining the authored per-bone
 * water, food, spoilage and dirt colors across GeckoLib 5's deferred submission.
 */
public class BirdBathRenderer extends GeoBlockRenderer<BirdBathBlockEntity, BlockEntityRenderState> {
    private static final DataTicket<BirdBathContentType> CONTENT =
            DataTicket.create("guaniao_bath_content", BirdBathContentType.class);
    private static final DataTicket<Integer> LEVEL = DataTicket.create("guaniao_bath_level", Integer.class);
    private static final DataTicket<BirdBathCleanliness> CLEANLINESS =
            DataTicket.create("guaniao_bath_cleanliness", BirdBathCleanliness.class);
    private static final DataTicket<BirdBathContentType> VISUAL_CONTENT =
            DataTicket.create("guaniao_bath_visual_content", BirdBathContentType.class);

    public BirdBathRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new BirdBathModel());
    }

    @Override
    public void addRenderData(BirdBathBlockEntity bath, Void unused, BlockEntityRenderState state, float partialTick) {
        super.addRenderData(bath, unused, state, partialTick);
        state.addGeckolibData(BirdBathModel.VARIANT, bath.variant());
        state.addGeckolibData(CONTENT, bath.getContentType());
        state.addGeckolibData(LEVEL, bath.getContentLevel());
        state.addGeckolibData(CLEANLINESS, bath.getCleanliness());
        state.addGeckolibData(VISUAL_CONTENT, bath.getRenderContentType());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<BlockEntityRenderState> info, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(info, snapshots);
        BirdBathContentType content = info.getOrDefaultGeckolibData(CONTENT, BirdBathContentType.EMPTY);
        int level = info.getOrDefaultGeckolibData(LEVEL, 0);
        BirdBathCleanliness cleanliness = info.getOrDefaultGeckolibData(CLEANLINESS, BirdBathCleanliness.CLEAN);
        BirdBathContentType visualContent = info.getOrDefaultGeckolibData(VISUAL_CONTENT, BirdBathContentType.EMPTY);
        for (GeoBone bone : info.model().topLevelBones()) {
            updateVisibility(snapshots, bone, content, level, cleanliness, visualContent);
        }
    }

    private static void updateVisibility(BoneSnapshots snapshots, GeoBone bone, BirdBathContentType content,
                                         int level, BirdBathCleanliness cleanliness, BirdBathContentType visualContent) {
        BirdBathBoneVisibility.apply(content, level, cleanliness, visualContent, snapshots.get(bone));
        for (GeoBone child : bone.children()) {
            updateVisibility(snapshots, child, content, level, cleanliness, visualContent);
        }
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<BlockEntityRenderState> info,
                                  OrderedSubmitNodeCollector tasks, RenderType renderType) {
        if (renderType == null) return;
        if (info.model().isMissingno()) {
            submitMissingModelRender(info, tasks);
            return;
        }

        int light = info.packedLight();
        int overlay = info.packedOverlay();
        int baseColor = info.renderColor();
        BirdBathContentType content = info.getOrDefaultGeckolibData(CONTENT, BirdBathContentType.EMPTY);
        int level = info.getOrDefaultGeckolibData(LEVEL, 0);
        BirdBathCleanliness cleanliness = info.getOrDefaultGeckolibData(CLEANLINESS, BirdBathCleanliness.CLEAN);
        BirdBathContentType visualContent = info.getOrDefaultGeckolibData(VISUAL_CONTENT, BirdBathContentType.EMPTY);

        // Match GeckoLib's deferred geometry pose and bone transforms, but
        // give each bone its authored tint instead of one color for the model.
        tasks.submitCustomGeometry(info.poseStack(), renderType, (pose, consumer) -> {
            PoseStack stack = info.poseStack();
            stack.pushPose();
            stack.last().set(pose);
            info.renderPosed(() -> {
                for (GeoBone bone : info.model().topLevelBones()) {
                    renderTintedBone(info, stack, consumer, bone, light, overlay, baseColor,
                            content, level, cleanliness, visualContent);
                }
            });
            stack.popPose();
        });
    }

    private static void renderTintedBone(RenderPassInfo<BlockEntityRenderState> info, PoseStack stack,
                                         VertexConsumer consumer, GeoBone bone, int light, int overlay, int baseColor,
                                         BirdBathContentType content, int level, BirdBathCleanliness cleanliness,
                                         BirdBathContentType visualContent) {
        stack.pushPose();
        RenderUtil.prepMatrixForBoneAndUpdateListeners(stack, bone, info);

        String boneName = bone.name();
        int color = baseColor;
        if (BirdBathBoneVisibility.isContentBoneVisible(content, level, visualContent, boneName)) {
            color = multiplyRgb(baseColor, BirdBathBoneVisibility.tintFor(content, visualContent, cleanliness));
        } else if (BirdBathBoneVisibility.isDirtBone(boneName)) {
            color = multiplyRgb(baseColor, BirdBathBoneVisibility.dirtTintFor(content, cleanliness, boneName));
        }
        bone.render(info, stack, consumer, light, overlay, color);
        if (bone.frameSnapshot == null || !bone.frameSnapshot.areChildrenHidden()) {
            for (GeoBone child : bone.children()) {
                renderTintedBone(info, stack, consumer, child, light, overlay, color,
                        content, level, cleanliness, visualContent);
            }
        }
        stack.popPose();
    }

    /** Multiply RGB like the original Forge renderRecursively hook; retain alpha. */
    static int multiplyRgb(int argb, float[] tint) {
        int red = Math.round(((argb >>> 16) & 0xFF) * tint[0]);
        int green = Math.round(((argb >>> 8) & 0xFF) * tint[1]);
        int blue = Math.round((argb & 0xFF) * tint[2]);
        return (argb & 0xFF000000) | (red << 16) | (green << 8) | blue;
    }

    @Override
    public boolean shouldRenderOffScreen(BirdBathBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return 48;
    }
}
