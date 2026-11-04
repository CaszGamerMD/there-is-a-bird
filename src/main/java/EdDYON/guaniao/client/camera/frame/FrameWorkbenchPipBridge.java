package EdDYON.guaniao.client.camera.frame;

import EdDYON.guaniao.mixin.GuaniaoGuiRenderStateAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

/**
 * Submit a 3D frame or workshop-bird render state to the official 26.2
 * picture-in-picture queue. The registered PiP renderer renders later,
 * after extraction, on the render thread into its depth-enabled target.
 *
 * This is infrastructure only: the authored frame mesh and animated bird
 * must be submitted using an actual registered Frame Workbench renderer
 * before the full screen can be considered migrated.
 */
final class FrameWorkbenchPipBridge {
    private FrameWorkbenchPipBridge() { }

    static void submit(GuiGraphicsExtractor graphics, PictureInPictureRenderState state) {
        if (state.bounds() == null) {
            return; // Fully clipped: do not create an offscreen render target.
        }
        ((GuaniaoGuiRenderStateAccess) graphics).guaniao$getGuiRenderState()
                .addPicturesInPictureState(state);
    }
}
