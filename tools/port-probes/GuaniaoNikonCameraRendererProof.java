package EdDYON.guaniao.proof;

import com.geckolib.constant.DataTickets;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.math.Axis;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

/** Compile Nikon's per-perspective GeckoLib 5 item pose without old preRender hooks. */
public final class GuaniaoNikonCameraRendererProof extends GeoItemRenderer<GuaniaoNikonCameraItemProof> {
    public GuaniaoNikonCameraRendererProof() {
        super(new GeoModel<>() {
            @Override
            public Identifier getModelResource(GeoRenderState state) {
                return Identifier.fromNamespaceAndPath("guaniao", "geo/nikon_d750");
            }
            @Override
            public Identifier getTextureResource(GeoRenderState state) {
                return Identifier.fromNamespaceAndPath("guaniao", "textures/item/nikon_d750.png");
            }
            @Override
            public Identifier getAnimationResource(GuaniaoNikonCameraItemProof animatable) {
                return Identifier.fromNamespaceAndPath("guaniao", "animations/nikon_d750");
            }
        });
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> info) {
        ItemDisplayContext perspective = info.renderState().getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE);
        if (perspective == ItemDisplayContext.GUI) {
            info.poseStack().translate(0.19F, -0.01F, 0.0F);
            info.poseStack().scale(0.37F, 0.37F, 0.37F);
        } else if (perspective == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            info.poseStack().mulPose(Axis.XP.rotationDegrees(-10.0F));
        }
        super.adjustRenderPose(info);
    }
}
