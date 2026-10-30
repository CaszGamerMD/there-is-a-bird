package EdDYON.guaniao.client.entity.seagull;

import EdDYON.guaniao.content.bird.seagull.SeagullEntity;
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

public final class SeagullHeldFoodLayer<R extends LivingEntityRenderState & GeoRenderState>
        extends BlockAndItemGeoLayer<SeagullEntity, Void, R> {
    private static final String FOOT_BONE = "foot_right";

    public SeagullHeldFoodLayer(EntityRendererProvider.Context context,
                               GeoRenderer<SeagullEntity, Void, R> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(SeagullEntity bird, Void relatedObject,
                                                R state, float partialTick) {
        ItemStack stack = bird.getHeldFoodForRendering();
        if (stack.isEmpty()) return List.of();
        return List.of(RenderData.item(FOOT_BONE, ItemDisplayContext.GROUND,
                RenderUtil.createRenderStateForItem(stack, this.itemModelResolver,
                        ItemDisplayContext.GROUND, bird)));
    }

    @Override
    public void addRenderData(SeagullEntity bird, Void relatedObject, R state, float partialTick) {
        List<RenderData> bones = getRelevantBones(bird, relatedObject, state, partialTick);
        if (!bones.isEmpty()) state.addGeckolibData(CONTENTS, bones);
    }

    @Override
    protected void submitItemStackRender(PoseStack poseStack, GeoBone bone,
                                         ItemStackRenderState stackState,
                                         ItemDisplayContext displayContext, R renderState,
                                         SubmitNodeCollector renderTasks, int packedLight) {
        poseStack.pushPose();
        try {
            // Anchor to the animated right foot instead of the beak.
            poseStack.translate(0.0D, 0.015D, -0.018D);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(145.0F));
            poseStack.scale(0.46F, 0.46F, 0.46F);
            super.submitItemStackRender(poseStack, bone, stackState, displayContext,
                    renderState, renderTasks, packedLight);
        } finally {
            poseStack.popPose();
        }
    }
}
