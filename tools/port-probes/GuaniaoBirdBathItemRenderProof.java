package EdDYON.guaniao.proof;

import com.geckolib.constant.DataTickets;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.math.Axis;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

/** Real 26.2 GeckoLib 5 render-state, inventory bone and authored resource API compilation. */
public final class GuaniaoBirdBathItemRenderProof extends GeoItemRenderer<GuaniaoBirdBathGeoItemProof> {
    public GuaniaoBirdBathItemRenderProof() {
        super(new GeoModel<>() {
            private final Identifier model = Identifier.fromNamespaceAndPath("guaniao", "geo/wooden_bird_bath");
            private final Identifier texture = Identifier.fromNamespaceAndPath("guaniao", "textures/block/wooden_bird_bath.png");
            private final Identifier animation = Identifier.fromNamespaceAndPath("guaniao", "animations/bird_bath");

            @Override
            public Identifier getModelResource(GeoRenderState renderState) { return model; }

            @Override
            public Identifier getTextureResource(GeoRenderState renderState) { return texture; }

            @Override
            public Identifier getAnimationResource(GuaniaoBirdBathGeoItemProof animatable) { return animation; }
        });
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> info) {
        if (info.renderState().getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE) == ItemDisplayContext.GUI) {
            info.poseStack().mulPose(Axis.XP.rotationDegrees(90.0F));
            info.poseStack().scale(0.8F, 0.8F, 0.8F);
        } else {
            super.adjustRenderPose(info);
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> info, BoneSnapshots snapshots) {
        snapshots.ifPresent("water_up", bone -> {
            bone.skipRender(true);
            bone.skipChildrenRender(true);
        });
    }
}
