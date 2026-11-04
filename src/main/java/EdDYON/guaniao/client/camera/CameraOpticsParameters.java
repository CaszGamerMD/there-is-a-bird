package EdDYON.guaniao.client.camera;

import EdDYON.guaniao.content.camera.CameraState;
import java.util.Objects;

/**
 * Exact author camera optics uniforms for the 26.2 post-processing migration.
 * Immutable snapshot: values cannot drift when the autofocus or zoom changes
 * between a rendered world frame and asynchronous screenshot readback.
 */
public record CameraOpticsParameters(
        float nearPlane,
        float farPlane,
        float focusDistance,
        float aperture,
        float focalLength,
        float dofMultiplier,
        float lensDistortion
) {
    public static final float NEAR_PLANE = 0.05F;

    public static CameraOpticsParameters fromCamera(CameraState state, float farPlane,
                                                     boolean depthOfFieldEnabled) {
        Objects.requireNonNull(state, "state");
        return new CameraOpticsParameters(
                NEAR_PLANE, farPlane,
                (float) state.focusDistance(),
                state.aperture().fStop(),
                (float) state.focalLength(),
                depthOfFieldEnabled ? state.lens().depthOfFieldMultiplier() : 0.0F,
                state.lens().distortion()
        );
    }

    public boolean needsOpticsPass() {
        return dofMultiplier != 0.0F || lensDistortion != 0.0F;
    }
}
