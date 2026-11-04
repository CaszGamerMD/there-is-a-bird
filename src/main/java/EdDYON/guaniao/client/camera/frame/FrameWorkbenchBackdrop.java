package EdDYON.guaniao.client.camera.frame;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Muted vanilla wood surfaces in the frame workbench. */
final class FrameWorkbenchBackdrop {
    private static final int TILE_SIZE=38;
    private static final float SHADE=0.48F;
    private static final Identifier WOOD=Identifier.withDefaultNamespace("textures/block/birch_planks.png");

    private FrameWorkbenchBackdrop() { }

    static void render(GuiGraphicsExtractor graphics) {
        render(graphics,79,41,152,150);
    }

    static void render(GuiGraphicsExtractor graphics,int x,int y,int width,int height) {
        // One continuous quad repeats the vanilla wood texture. Separate atlas-sprite
        // tiles expose their sampling boundaries when the workbench uses a fractional scale.
        // 26.2's extractor carries tint on the submitted textured quad.
        // 0x7a is round(255 * the original 0.48 wood shading).
        graphics.blit(RenderPipelines.GUI_TEXTURED, WOOD,
                x, y, 0.0F, 0.0F, width, height, TILE_SIZE, TILE_SIZE, 0xFF7A7A7A);
        graphics.fillGradient(x,y,x+width,y+height,0x08000000,0x28000000);
        graphics.fill(x,y,x+width,y+1,0x48000000);
        graphics.fill(x,y,x+1,y+height,0x48000000);
    }
}
