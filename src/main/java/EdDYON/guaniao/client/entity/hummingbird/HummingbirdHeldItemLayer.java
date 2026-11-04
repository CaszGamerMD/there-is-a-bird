package EdDYON.guaniao.client.entity.hummingbird;

import EdDYON.guaniao.content.bird.hummingbird.HummingbirdEntity;
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

/** The synchronized garden item remains at the original head-local bill tip. */
public final class HummingbirdHeldItemLayer<R extends LivingEntityRenderState & GeoRenderState>
        extends BlockAndItemGeoLayer<HummingbirdEntity, Void, R> {
    public HummingbirdHeldItemLayer(EntityRendererProvider.Context context,
                                   GeoRenderer<HummingbirdEntity, Void, R> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(HummingbirdEntity bird, Void relatedObject,
                                                 R renderState, float partialTick) {
        ItemStack held = bird.getHeldGardenItem();
        if (held.isEmpty()) return List.of();
        return List.of(RenderData.item("head", ItemDisplayContext.NONE,
                RenderUtil.createRenderStateForItem(held, this.itemModelResolver,
                        ItemDisplayContext.NONE, bird)));
    }

    @Override
    public void addRenderData(HummingbirdEntity bird, Void relatedObject, R renderState,
                              float partialTick) {
        List<RenderData> bones = getRelevantBones(bird, relatedObject, renderState, partialTick);
        if (!bones.isEmpty()) renderState.addGeckolibData(CONTENTS, bones);
    }

    @Override
    protected void submitItemStackRender(PoseStack pose, GeoBone bone,
                                         ItemStackRenderState stackState, ItemDisplayContext displayContext,
                                         R renderState, SubmitNodeCollector renderTasks, int packedLight) {
        pose.pushPose();
        try {
            pose.translate(0, 4.25 / 16.0, -3.45 / 16.0);
            pose.mulPose(Axis.ZP.rotationDegrees(-20));
            pose.scale(0.12F, 0.12F, 0.12F);
            super.submitItemStackRender(pose, bone, stackState, displayContext,
                    renderState, renderTasks, packedLight);
        } finally {
            pose.popPose();
        }
    }
}
