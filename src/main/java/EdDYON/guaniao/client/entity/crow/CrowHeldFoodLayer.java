package EdDYON.guaniao.client.entity.crow;

import EdDYON.guaniao.content.bird.crow.CrowEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
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

public class CrowHeldFoodLayer<R extends LivingEntityRenderState & GeoRenderState>
        extends BlockAndItemGeoLayer<CrowEntity, Void, R> {
    private static final String MOUTH_BONE = "mouth_anchor";
    private static final double MOUTH_ANCHOR_Y = 12.475D / 16.0D;
    private static final double MOUTH_ANCHOR_Z = -8.95D / 16.0D;
    private static final float BASE_X_ROTATION = 90.0F;
    private static final float BASE_Z_ROTATION = 150.0F;
    private static final float X_ROTATION_VARIATION = 5.0F;
    private static final float Z_ROTATION_VARIATION = 35.0F;
    private static final float ITEM_SCALE = 0.80F;
    private static final DataTicket<Integer> POSE_SEED =
            DataTicket.create("guaniao_crow_held_food_pose_seed", Integer.class);

    public CrowHeldFoodLayer(EntityRendererProvider.Context context,
                             GeoRenderer<CrowEntity, Void, R> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(CrowEntity crow, Void relatedObject,
                                                 R renderState, float partialTick) {
        ItemStack stack = getStackForRendering(crow);
        if (stack.isEmpty()) return List.of();
        renderState.addGeckolibData(POSE_SEED, getPoseSeed(crow, stack));
        return List.of(RenderData.item(MOUTH_BONE, ItemDisplayContext.GROUND,
                RenderUtil.createRenderStateForItem(stack, this.itemModelResolver,
                        ItemDisplayContext.GROUND, crow)));
    }

    @Override
    public void addRenderData(CrowEntity crow, Void relatedObject, R renderState, float partialTick) {
        List<RenderData> bones = getRelevantBones(crow, relatedObject, renderState, partialTick);
        if (!bones.isEmpty()) renderState.addGeckolibData(CONTENTS, bones);
    }

    @Override
    protected void submitItemStackRender(PoseStack poseStack, GeoBone bone,
                                         ItemStackRenderState stackState,
                                         ItemDisplayContext displayContext, R renderState,
                                         SubmitNodeCollector renderTasks, int packedLight) {
        int poseSeed = renderState.getOrDefaultGeckolibData(POSE_SEED, 0);
        poseStack.pushPose();
        try {
            poseStack.translate(0.0D, MOUTH_ANCHOR_Y - 0.015D, MOUTH_ANCHOR_Z - 0.025D);
            poseStack.mulPose(Axis.XP.rotationDegrees(BASE_X_ROTATION + seededOffset(poseSeed, 11, X_ROTATION_VARIATION)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(BASE_Z_ROTATION + seededOffset(poseSeed, 37, Z_ROTATION_VARIATION)));
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            super.submitItemStackRender(poseStack, bone, stackState, displayContext,
                    renderState, renderTasks, packedLight);
        } finally {
            poseStack.popPose();
        }
    }

    private static int getPoseSeed(CrowEntity crow, ItemStack stack) {
        ItemStack heldFood = crow.getHeldFoodForRendering();
        int seed = crow.getHeldFoodPoseSeed();
        if (!heldFood.isEmpty() && seed != 0) return seed;
        return 31 * crow.getId() + stack.getDescriptionId().hashCode();
    }

    private static float seededOffset(int seed, int salt, float range) {
        return (seededUnit(seed, salt) * 2.0F - 1.0F) * range;
    }

    private static float seededUnit(int seed, int salt) {
        int value = seed ^ salt * 0x9E3779B9;
        value ^= value >>> 16;
        value *= 0x7FEB352D;
        value ^= value >>> 15;
        value *= 0x846CA68B;
        value ^= value >>> 16;
        return (value & 0xFFFFFF) / 16777216.0F;
    }

    private static ItemStack getStackForRendering(CrowEntity crow) {
        ItemStack stack = crow.getHeldFoodForRendering();
        if (!stack.isEmpty()) return stack;
        return ItemStack.EMPTY;
    }
}
