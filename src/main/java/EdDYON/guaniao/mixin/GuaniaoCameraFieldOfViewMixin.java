package EdDYON.guaniao.mixin;

import EdDYON.guaniao.client.camera.CameraClientCapture;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fabric 26.2 successor to Forge ViewportEvent.ComputeFov.
 *
 * 26.2 computes the WORLD FOV on Camera.calculateFov(float), separately
 * from Camera.calculateHudFov. Avoid touching HUD, item or screenshot
 * overlays that use their own projections. The existing camera state
 * chooses the Nikon focal zoom or a frozen clean-shutter focal length.
 */
@Mixin(Camera.class)
public abstract class GuaniaoCameraFieldOfViewMixin {
    @ModifyReturnValue(method = "calculateFov(F)F", at = @At("RETURN"), require = 1)
    private float guaniao$cameraFov(float vanillaFov) {
        return CameraClientCapture.overrideWorldFov(vanillaFov);
    }
}
