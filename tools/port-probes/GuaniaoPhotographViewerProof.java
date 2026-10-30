package EdDYON.guaniao.fabric;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Compile-only proof of the actual full-UV photograph blit in 26.2. */
public final class GuaniaoPhotographViewerProof {
    private GuaniaoPhotographViewerProof() { }

    public static void submit(GuiGraphicsExtractor graphics, Identifier photograph,
                              int x, int y, int size) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, photograph,
                x, y, 0.0F, 0.0F, size, size, size, size);
    }
}
