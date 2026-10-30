package EdDYON.guaniao.fabric;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Compile-only contract for 26.2 screen events without touching old rendering. */
public final class GuaniaoCameraScreenInputProof extends Screen {
    public GuaniaoCameraScreenInputProof() {
        super(Component.literal("Camera input API proof"));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int button = event.button();
        double x = event.x();
        double y = event.y();
        return button >= 0 && x >= 0 && y >= 0 || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return event.key() == -1 || super.keyPressed(event);
    }
}
