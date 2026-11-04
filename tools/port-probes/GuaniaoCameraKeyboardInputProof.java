package EdDYON.guaniao.fabric;

import net.minecraft.client.input.KeyEvent;

/** Compile-only proof of native Minecraft 26.2 keyboard event fields. */
public final class GuaniaoCameraKeyboardInputProof {
    private GuaniaoCameraKeyboardInputProof() { }

    public static int[] forward(KeyEvent event, int action) {
        return new int[] {event.key(), event.scancode(), action, event.modifiers()};
    }
}
