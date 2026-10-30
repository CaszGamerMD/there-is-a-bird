package EdDYON.guaniao.mixin;

import EdDYON.guaniao.client.camera.CameraClientCapture;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * In-world camera keyboard lifecycle replacing the Forge InputEvent.Key hook.
 * The vanilla handler still receives EVERY key, including Escape, screenshots
 * and modifier shortcuts. This hook only suspends camera shutter input or
 * closes an active camera viewfinder; it never cancels Minecraft keybindings.
 */
@Mixin(KeyboardHandler.class)
public abstract class GuaniaoCameraKeyboardInputMixin {
    @Inject(method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V",
            at = @At("HEAD"), require = 1)
    private void guaniao$rawKey(long window, int action, KeyEvent input,
                                CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getWindow().getWindow() != window) return;

        // Give screenshot and Ctrl/Alt/Super shortcuts priority to suspend
        // the shutter BEFORE vanilla processes the same input event.
        CameraClientCapture.handleKeyInput(
                input.key(), input.scancode(), action, input.modifiers());

        if (action != GLFW.GLFW_PRESS || input.key() != GLFW.GLFW_KEY_ESCAPE
                || !CameraClientCapture.isViewfinderOpen()) return;

        // Preserve the original Forge "no other control screen" rule.
        // On this early raw hook vanilla has not yet opened PauseScreen;
        // closing the camera must not swallow Escape or alter that behavior.
        var screen = minecraft.gui.screen();
        if (screen == null || screen instanceof PauseScreen) {
            CameraClientCapture.closeViewfinder();
        }
    }
}
