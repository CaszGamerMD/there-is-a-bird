package EdDYON.guaniao.fabric;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Compile-only 26.2 API proof; never registered as a real screen. */
public final class GuaniaoCameraHelpExtractionProof {
    private GuaniaoCameraHelpExtractionProof() { }

    public static void draw(GuiGraphicsExtractor graphics, Font font,
                            Component heading, FormattedCharSequence footer,
                            int x, int y) {
        graphics.fill(x, y, x + 240, y + 128, 0xFFC6C6C6);
        graphics.horizontalLine(x, x + 239, y, 0xFFFFFFFF);
        graphics.verticalLine(x, y, y + 127, 0xFFFFFFFF);
        graphics.text(font, heading, x + 12, y + 8, 0xFF303030, false);
        graphics.text(font, footer, x + 12, y + 79, 0xFF555555, false);
        graphics.setTooltipForNextFrame(font, heading, x + 12, y + 28);
    }
}
