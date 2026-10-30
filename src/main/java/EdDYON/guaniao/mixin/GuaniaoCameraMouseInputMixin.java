package EdDYON.guaniao.mixin;

import EdDYON.guaniao.client.camera.CameraClientCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * In-world camera input replacing Forge InputEvent.MouseButton.Pre and
 * InputEvent.MouseScrollingEvent. Only installed on the Fabric client.
 *
 * A raw input hook is required: ordinary screen events cannot intercept
 * a grabbed, in-world mouse before vanilla attacks, uses or hotbar scroll.
 * Respect the active window; the original CameraClientCapture state machine
 * handles screen focus, control/alt shortcuts, click-release arming, pending
 * screenshots, and capture-vs-close behavior without any duplicated policy.
 */
@Mixin(MouseHandler.class)
public abstract class GuaniaoCameraMouseInputMixin {
    @Inject(method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V",
            at = @At("HEAD"), cancellable = true, require = 1)
    private void guaniao$mouseButton(long window, MouseButtonInfo buttonInfo,
                                    int action, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getWindow().getWindow() != window) return;
        // Releases are passed to the state machine too, even when the camera
        // is no longer open; they are never consumed from vanilla.
        if (CameraClientCapture.handleMouseButton(
                buttonInfo.button(), action, buttonInfo.modifiers())) {
            ci.cancel();
        }
    }

    @Inject(method = "onScroll(JDD)V",
            at = @At("HEAD"), cancellable = true, require = 1)
    private void guaniao$mouseScroll(long window, double horizontal,
                                    double vertical, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getWindow().getWindow() != window) return;
        // The original camera uses the vertical wheel only. Screen and
        // shortcut guards remain inside handleMouseScroll, so normal
        // inventory scrolling is not intercepted by this mixin.
        if (CameraClientCapture.handleMouseScroll(vertical)) {
            ci.cancel();
        }
    }
}
