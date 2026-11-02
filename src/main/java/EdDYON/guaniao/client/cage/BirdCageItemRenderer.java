package EdDYON.guaniao.client.cage;

import EdDYON.guaniao.content.cage.BirdCageItem;
import EdDYON.guaniao.content.cage.BirdCageVariant;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;

public class BirdCageItemRenderer extends GeoItemRenderer<BirdCageItem> {
    private final BirdCageVariant variant;

    public BirdCageItemRenderer(BirdCageItem item) {
        super(new BirdCageItemModel(item.variant()));
        this.variant = item.variant();
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> info) {
        PoseStack poseStack = info.poseStack();
        poseStack.translate(itemOffsetX(this.variant), itemOffsetY(this.variant), 0.0F);
        float scale = itemScale(this.variant);
        poseStack.scale(scale, scale, scale);
        super.adjustRenderPose(info);
    }

    private static float itemScale(BirdCageVariant variant) {
        return switch (variant) {
            case SMALL -> 0.78F;
            case MEDIUM -> 0.30F;
            case LARGE -> 0.22F;
        };
    }

    private static float itemOffsetX(BirdCageVariant variant) {
        return switch (variant) {
            case SMALL -> 0.08F;
            case MEDIUM -> 0.30F;
            case LARGE -> 0.37F;
        };
    }

    private static float itemOffsetY(BirdCageVariant variant) {
        return switch (variant) {
            case SMALL -> -0.40F;
            case MEDIUM -> -0.10F;
            case LARGE -> -0.10F;
        };
    }
}
