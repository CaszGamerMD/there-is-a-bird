package EdDYON.guaniao.client.entity.woodcock;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.content.bird.woodcock.WoodcockDefinition;
import EdDYON.guaniao.content.bird.woodcock.WoodcockEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Avoid GeckoLib's blend-time root-size flicker: the authored 0.45 scale
 * belongs to each render pass, not a shared bone or animation frame.
 * Flying birds display the fly wing rig; grounded birds display both wings.
 */
public class WoodcockRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<WoodcockEntity, R> {
    public WoodcockRenderer(EntityRendererProvider.Context context) {
        super(context, new WoodcockModel());
        this.shadowRadius = 0.16F;
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo,
                                    float widthScale, float heightScale) {
        float scale = renderPassInfo.renderState().getOrDefaultGeckolibData(
                BirdMutationRenderData.MODEL_SCALE, 1.0F);
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        snapshots.get("root_shape").ifPresent(root -> root.setScale(
                WoodcockDefinition.MODEL_ROOT_SCALE,
                WoodcockDefinition.MODEL_ROOT_SCALE,
                WoodcockDefinition.MODEL_ROOT_SCALE));

        boolean flying = renderPassInfo.getOrDefaultGeckolibData(WoodcockModel.FLYING, false);
        snapshots.get("wing_left").ifPresent(wing -> {
            wing.skipRender(flying);
            wing.skipChildrenRender(flying);
        });
        snapshots.get("wing_right").ifPresent(wing -> {
            wing.skipRender(flying);
            wing.skipChildrenRender(flying);
        });
        snapshots.get("fly").ifPresent(wing -> {
            wing.skipRender(!flying);
            wing.skipChildrenRender(!flying);
        });
    }
}
