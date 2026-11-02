package EdDYON.guaniao.client.bath;

import EdDYON.guaniao.content.bath.BirdBathItem;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.Set;

/** GeckoLib 5's render-state equivalent of the original bird bath inventory presentation. */
public class BirdBathItemRenderer extends GeoItemRenderer<BirdBathItem> {
    // Inventory baths are always empty and clean. These are exactly the authored
    // content and dirt bones that the old renderRecursively callback suppressed.
    private static final Set<String> HIDDEN_BONES = Set.of(
            "water_up", "water_middle", "water_down",
            "ice_up", "ice_middle", "ice_down",
            "fish_up", "fish_middle", "fish_down",
            "meat_up", "meat_middle", "meat_down",
            "bread_up", "bread_middle", "bread_down",
            "spoiled_up", "spoiled_middle", "spoiled_down",
            "dirty_spots_light", "dirty_spots_medium", "dirty_spots_heavy",
            "spoil_spots", "flies"
    );

    public BirdBathItemRenderer(BirdBathItem item) {
        super(new BirdBathItemModel(item.variant()));
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> info) {
        PoseStack poseStack = info.poseStack();
        if (info.renderState().getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE) == ItemDisplayContext.GUI) {
            // Authored slot view: show the basin top while keeping the small birds
            // on the second design visible. The old preRender canceled GeckoLib's
            // default origin translation here, so do not apply it in this branch.
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.8F, 0.8F, 0.8F);
            poseStack.translate(0.0F, -1.4F, 0.0F);
        } else {
            poseStack.translate(itemOffsetX(), itemOffsetY(), itemOffsetZ());
            float scale = itemScale();
            poseStack.scale(scale, scale, scale);
            super.adjustRenderPose(info);
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> info, BoneSnapshots snapshots) {
        for (String name : HIDDEN_BONES) {
            snapshots.ifPresent(name, bone -> {
                bone.skipRender(true);
                bone.skipChildrenRender(true);
            });
        }
    }

    private static float itemOffsetX() {
        return 0.28F;
    }

    private static float itemOffsetY() {
        return -0.12F;
    }

    private static float itemOffsetZ() {
        return 0.0F;
    }

    private static float itemScale() {
        return 0.5F;
    }
}
