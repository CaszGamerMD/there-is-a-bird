package EdDYON.guaniao.client.camera.frame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.LightCoordsUtil;

/**
 * Actual Minecraft 26.2 depth-buffered renderer for the Frame Workbench's
 * original wood/rim/lining/trim silhouette. Color and quad topology come
 * from the original author. Only the old immediate-mode submission changes.
 *
 * Photograph and sticker images, the assembly animation and observer bird
 * require separate deferred texture/entity passes before GUI integration.
 */
public final class FrameWorkbenchPreview3DRenderer
        extends PictureInPictureRenderer<FrameWorkbenchPreview3DState> {
    @Override
    public Class<FrameWorkbenchPreview3DState> getRenderStateClass() {
        return FrameWorkbenchPreview3DState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "guaniao frame workbench preview";
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        // The original frame coordinates have their origin in the centre,
        // not the feet of an inventory-preview entity.
        return height / 2.0F;
    }

    @Override
    protected void renderToTexture(FrameWorkbenchPreview3DState state,
                                   PoseStack pose, SubmitNodeCollector collector) {
        if (state.mesh() == null) return;
        pose.pushPose();
        try {
            pose.mulPose(Axis.XP.rotationDegrees(state.pitch()));
            pose.mulPose(Axis.YP.rotationDegrees(state.yaw()));

            // Preserve the original layer order, backing on BOTH faces,
            // wood frame on every occupied silhouette cell, darker inner
            // rims and the optional decorative trim at exterior edges.
            var mesh=state.mesh();
            var lining=state.lining();
            var wood=state.wood();
            var trim=state.trim();
            int light=LightCoordsUtil.FULL_BRIGHT;
            if (lining != null) {
                FrameWorkbenchMeshSubmitter.submit(mesh.backing(), lining.sprite(),
                        pose, collector, light, lining.red(), lining.green(),
                        lining.blue(), 1.0F, lining.translucent());
            }
            if (wood != null) {
                FrameWorkbenchMeshSubmitter.submit(mesh.wood(), wood.sprite(),
                        pose, collector, light, wood.red(), wood.green(),
                        wood.blue(), 1.0F, wood.translucent());
                FrameWorkbenchMeshSubmitter.submit(mesh.rim(), wood.sprite(),
                        pose, collector, light, wood.red()*0.62F, wood.green()*0.60F,
                        wood.blue()*0.55F, 1.0F, wood.translucent());
            }
            if (trim != null) {
                FrameWorkbenchMeshSubmitter.submit(mesh.trim(), trim.sprite(),
                        pose, collector, light, trim.red(), trim.green(),
                        trim.blue(), 1.0F, trim.translucent());
            }
        } finally {
            pose.popPose();
        }
    }
}
