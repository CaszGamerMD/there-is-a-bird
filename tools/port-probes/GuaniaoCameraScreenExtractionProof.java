package EdDYON.guaniao.fabric;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Compile-only proof of real Minecraft 26.2 deferred GUI primitives. */
public final class GuaniaoCameraScreenExtractionProof {
    private GuaniaoCameraScreenExtractionProof() { }

    public static void draw(GuiGraphicsExtractor graphics, Font font, Component label) {
        graphics.fill(0, 0, 64, 64, 0xCC24292D);
        graphics.horizontalLine(0, 64, 0, 0xFFABCD12);
        graphics.verticalLine(0, 0, 64, 0xFFABCD12);
        graphics.enableScissor(1, 1, 63, 63);
        graphics.text(font, label, 4, 4, 0xFFFFFFFF, false);
        graphics.centeredText(font, label, 32, 22, 0xFFFFFFFF);
        graphics.pose().pushMatrix();
        graphics.pose().translate(4.0F, 28.0F);
        graphics.pose().scale(0.8F, 0.8F);
        graphics.text(font, label, 0, 0, 0xFFFFFFFF, false);
        graphics.pose().popMatrix();
        graphics.disableScissor();
    }
}
