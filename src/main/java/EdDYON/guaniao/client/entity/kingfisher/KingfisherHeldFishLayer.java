package EdDYON.guaniao.client.entity.kingfisher;

import EdDYON.guaniao.content.bird.kingfisher.KingfisherEntity;
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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Keep the server-synchronized fish at the animated bill during swallowing. */
public final class KingfisherHeldFishLayer<R extends LivingEntityRenderState & GeoRenderState>
        extends BlockAndItemGeoLayer<KingfisherEntity, Void, R> {
    private static final DataTicket<Float> SWALLOWING =
            DataTicket.create("guaniao_kingfisher_swallowing", Float.class);

    public KingfisherHeldFishLayer(EntityRendererProvider.Context context,
                                   GeoRenderer<KingfisherEntity, Void, R> renderer) {
        super(context, renderer);
    }

    @Override protected List<RenderData> getRelevantBones(KingfisherEntity bird, Void relatedObject,
                                                            R renderState, float partialTick) {
        ItemStack held = bird.getHeldFish();
        if (held.isEmpty()) return List.of();
        float swallowing = bird.isEating() ? Mth.clamp((bird.getEatingTicks(partialTick) - 32) / 15, 0, 1) : 0;
        swallowing = swallowing * swallowing * (3 - 2 * swallowing);
        if (swallowing >= 0.999F) return List.of();
        renderState.addGeckolibData(SWALLOWING, swallowing);
        return List.of(RenderData.item("head", ItemDisplayContext.NONE,
                RenderUtil.createRenderStateForItem(held, this.itemModelResolver, ItemDisplayContext.NONE, bird)));
    }

    @Override public void addRenderData(KingfisherEntity bird, Void relatedObject, R renderState, float partialTick) {
        List<RenderData> bones = getRelevantBones(bird, relatedObject, renderState, partialTick);
        if (!bones.isEmpty()) renderState.addGeckolibData(CONTENTS, bones);
    }

    @Override protected void submitItemStackRender(PoseStack pose, GeoBone bone,
                                                     ItemStackRenderState stackState,
                                                     ItemDisplayContext displayContext, R renderState,
                                                     SubmitNodeCollector renderTasks, int packedLight) {
        float swallowing = renderState.getOrDefaultGeckolibData(SWALLOWING, 0.0F);
        pose.pushPose();
        try {
            pose.translate(0, (6.35D + 0.25D * swallowing) / 16.0D,
                    (-6.05D + 1.65D * swallowing) / 16.0D);
            pose.mulPose(Axis.ZP.rotationDegrees(-20 - 45 * swallowing));
            float size = 0.17F * (1 - swallowing);
            pose.scale(size, size, size);
            super.submitItemStackRender(pose, bone, stackState, displayContext, renderState, renderTasks, packedLight);
        } finally {
            pose.popPose();
        }
    }
}
