package EdDYON.guaniao.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 26.2 GUI extraction owns the render-state queue privately. Custom 3D
 * Frame Workbench previews will be submitted as PiP render states, not
 * rendered immediately with an old GuiGraphics/PoseStack.
 */
@Mixin(GuiGraphicsExtractor.class)
public interface GuaniaoGuiRenderStateAccess {
    @Accessor("guiRenderState")
    GuiRenderState guaniao$getGuiRenderState();
}
