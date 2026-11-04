package EdDYON.guaniao.fabric;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Compile-only 26.2 HUD text/pose APIs; never registered to replace a real lens. */
public final class GuaniaoCameraViewfinderHudProof {
    private GuaniaoCameraViewfinderHudProof() { }

    public static void extract(GuiGraphicsExtractor graphics, Font font, String status,
                               int left, int top, int right, int bottom, float scale) {
        graphics.fill(left, top, right, top + 1, 0xFF363632);
        graphics.fill(left, bottom - 1, right, bottom, 0xFF363632);
        graphics.pose().pushMatrix();
        graphics.pose().translate(left, top);
        graphics.pose().scale(scale, scale);
        graphics.text(font, status, 0, 0, 0xFFEBE8D8, false);
        graphics.pose().popMatrix();
    }
}
