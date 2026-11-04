package EdDYON.guaniao.client.entity.kestrel;

import EdDYON.guaniao.content.bird.kestrel.KestrelEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** The carried item stays on the talons at the original root-relative pose. */
public final class KestrelHeldItemLayer<R extends LivingEntityRenderState & GeoRenderState>
        extends BlockAndItemGeoLayer<KestrelEntity, Void, R> {
    private static final String TALON_ANCHOR_BONE = "root";

    public KestrelHeldItemLayer(EntityRendererProvider.Context context,
                                GeoRenderer<KestrelEntity, Void, R> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(KestrelEntity bird, Void relatedObject,
                                                 R renderState, float partialTick) {
        ItemStack stack = bird.getCarriedItem();
        if (stack.isEmpty()) return List.of();
        return List.of(RenderData.item(TALON_ANCHOR_BONE, ItemDisplayContext.GROUND,
                RenderUtil.createRenderStateForItem(stack, this.itemModelResolver,
                        ItemDisplayContext.GROUND, bird)));
    }

    @Override
    public void addRenderData(KestrelEntity bird, Void relatedObject, R renderState,
                              float partialTick) {
        List<RenderData> bones = getRelevantBones(bird, relatedObject, renderState, partialTick);
        if (!bones.isEmpty()) renderState.addGeckolibData(CONTENTS, bones);
    }

    @Override
    protected void submitItemStackRender(PoseStack poseStack, GeoBone bone,
                                         ItemStackRenderState stackState,
                                         ItemDisplayContext displayContext, R renderState,
                                         SubmitNodeCollector renderTasks, int packedLight) {
        poseStack.pushPose();
        try {
            // Talons meet below and ahead of model root; head-look must not move item.
            poseStack.translate(0.0D, 0.018D, -0.095D);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(135.0F));
            poseStack.scale(0.46F, 0.46F, 0.46F);
            super.submitItemStackRender(poseStack, bone, stackState, displayContext,
                    renderState, renderTasks, packedLight);
        } finally {
            poseStack.popPose();
        }
    }
}
