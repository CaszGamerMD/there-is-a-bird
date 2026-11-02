package EdDYON.guaniao.client.nest;

import EdDYON.guaniao.content.nest.CrowNestItem;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;

public class CrowNestItemRenderer extends GeoItemRenderer<CrowNestItem> {
    private static final Set<String> EGG_BONES = Set.of("egg1", "egg2", "egg3");

    public CrowNestItemRenderer() {
        super(new CrowNestItemModel());
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> info) {
        PoseStack poseStack = info.poseStack();
        poseStack.translate(0.25F, -0.18F, 0.0F);
        poseStack.scale(0.62F, 0.62F, 0.62F);
        super.adjustRenderPose(info);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> info, BoneSnapshots snapshots) {
        // A nest in inventory never contains the placed nest's one-to-three eggs.
        for (String name : EGG_BONES) {
            snapshots.ifPresent(name, bone -> {
                bone.skipRender(true);
                bone.skipChildrenRender(true);
            });
        }
    }
}
