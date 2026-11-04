package EdDYON.guaniao.client.entity.nightheron;

import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
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

/** Keep the fish in front of the bill while switching to the small delivery pose. */
public class NightHeronHeldFishLayer<R extends LivingEntityRenderState & GeoRenderState>
        extends BlockAndItemGeoLayer<NightHeronEntity, Void, R> {
    private static final String MOUTH_BONE = "fish_anchor";
    private static final double MOUTH_ANCHOR_Y = 14.35D / 16.0D;
    private static final double MOUTH_ANCHOR_Z = -12.85D / 16.0D;
    private static final float BASE_X_ROTATION = 90.0F;
    private static final float BASE_Z_ROTATION = 145.0F;
    private static final float X_ROTATION_VARIATION = 4.0F;
    private static final float Z_ROTATION_VARIATION = 22.0F;
    private static final float ITEM_SCALE = 0.55F;
    // The bill ends at local Z = -13.10648; keep the carried sprite just beyond its tip.
    private static final double DELIVERY_ANCHOR_Y = 14.05D / 16.0D;
    private static final double DELIVERY_ANCHOR_Z = -13.35D / 16.0D;
    private static final float DELIVERY_SCALE = 0.34F;
    private static final DataTicket<Integer> POSE_SEED =
            DataTicket.create("guaniao_night_heron_held_fish_pose_seed", Integer.class);
    private static final DataTicket<Boolean> DELIVERY =
            DataTicket.create("guaniao_night_heron_fish_delivery", Boolean.class);

    public NightHeronHeldFishLayer(EntityRendererProvider.Context context,
                                   GeoRenderer<NightHeronEntity, Void, R> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(NightHeronEntity bird, Void relatedObject,
                                                  R renderState, float partialTick) {
        ItemStack stack = getStackForRendering(bird);
        if (stack.isEmpty()) return List.of();
        boolean delivery = bird.hasDeliveryFish();
        renderState.addGeckolibData(POSE_SEED, getPoseSeed(bird, stack));
        renderState.addGeckolibData(DELIVERY, delivery);
        ItemDisplayContext context = delivery ? ItemDisplayContext.NONE : ItemDisplayContext.GROUND;
        return List.of(RenderData.item(MOUTH_BONE, context,
                RenderUtil.createRenderStateForItem(stack, this.itemModelResolver, context, bird)));
    }

    @Override
    public void addRenderData(NightHeronEntity bird, Void relatedObject,
                              R renderState, float partialTick) {
        List<RenderData> bones = getRelevantBones(bird, relatedObject, renderState, partialTick);
        if (!bones.isEmpty()) renderState.addGeckolibData(CONTENTS, bones);
    }

    @Override
    protected void submitItemStackRender(PoseStack poseStack, GeoBone bone,
                                         ItemStackRenderState stackState,
                                         ItemDisplayContext displayContext, R renderState,
                                         SubmitNodeCollector renderTasks, int packedLight) {
        int poseSeed = renderState.getOrDefaultGeckolibData(POSE_SEED, 0);
        boolean delivery = renderState.getOrDefaultGeckolibData(DELIVERY, false);
        poseStack.pushPose();
        try {
            poseStack.translate(0.0D, delivery ? DELIVERY_ANCHOR_Y : MOUTH_ANCHOR_Y,
                    delivery ? DELIVERY_ANCHOR_Z : MOUTH_ANCHOR_Z);
            // Keep the fish's depth fixed in front of the bill; tilting it can cut back through the beak.
            poseStack.mulPose(Axis.XP.rotationDegrees(delivery ? 0 : BASE_X_ROTATION + seededOffset(poseSeed, 11, X_ROTATION_VARIATION)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(delivery ? -20 : BASE_Z_ROTATION + seededOffset(poseSeed, 37, Z_ROTATION_VARIATION)));
            float scale = delivery ? DELIVERY_SCALE : ITEM_SCALE;
            poseStack.scale(scale, scale, scale);
            super.submitItemStackRender(poseStack, bone, stackState, displayContext,
                    renderState, renderTasks, packedLight);
        } finally {
            poseStack.popPose();
        }
    }

    private static int getPoseSeed(NightHeronEntity bird, ItemStack stack) {
        int seed = bird.getHeldFishPoseSeed();
        if (!bird.getHeldFishForRendering().isEmpty() && seed != 0) return seed;
        return 31 * bird.getId() + stack.getDescriptionId().hashCode();
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

    private static ItemStack getStackForRendering(NightHeronEntity bird) {
        ItemStack stack = bird.getHeldFishForRendering();
        if (NightHeronEntity.isEdibleFishItem(stack)) return stack;
        return ItemStack.EMPTY;
    }
}
