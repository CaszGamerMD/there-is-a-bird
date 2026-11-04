package EdDYON.guaniao.client.camera;

import EdDYON.guaniao.content.camera.CameraAperture;
import EdDYON.guaniao.content.camera.CameraLens;
import EdDYON.guaniao.content.camera.CameraState;

/** Standalone author-uniform mapping regression: no Minecraft runtime needed. */
public final class GuaniaoCameraOpticsParametersRegression {
    private GuaniaoCameraOpticsParametersRegression() { }

    private static void exact(float actual, float expected, String label) {
        if (Float.floatToIntBits(actual) != Float.floatToIntBits(expected)) {
            throw new AssertionError(label + ": got " + actual + ", expected " + expected);
        }
    }

    public static void main(String[] args) {
        CameraState original = CameraState.defaults();
        CameraOpticsParameters defaults = CameraOpticsParameters.fromCamera(original, 512.0F, true);
        exact(defaults.nearPlane(), 0.05F, "near");
        exact(defaults.farPlane(), 512.0F, "far");
        exact(defaults.focusDistance(), 12.0F, "focus");
        exact(defaults.aperture(), 5.6F, "aperture");
        exact(defaults.focalLength(), 50.0F, "focal");
        exact(defaults.dofMultiplier(), 1.0F, "original normal lens");
        exact(defaults.lensDistortion(), 0.0F, "standard distortion");
        if (!defaults.needsOpticsPass()) throw new AssertionError("Default DoF pass missing");

        CameraOpticsParameters off = CameraOpticsParameters.fromCamera(original, 384.0F, false);
        exact(off.dofMultiplier(), 0.0F, "disabled DoF");
        exact(off.farPlane(), 384.0F, "world render far plane");
        if (off.needsOpticsPass()) throw new AssertionError("Standard lens with DoF off should bypass");

        for (CameraLens lens : CameraLens.values()) {
            CameraState state = original.withLens(lens);
            for (CameraAperture aperture : CameraAperture.values()) {
                state = state.withAperture(aperture);
                CameraOpticsParameters params = CameraOpticsParameters.fromCamera(state, 256.0F, true);
                exact(params.aperture(), aperture.fStop(), "authored F-stop");
                exact(params.focalLength(), (float)state.focalLength(), "authored focal");
                exact(params.dofMultiplier(), lens.depthOfFieldMultiplier(), "lens DoF");
                exact(params.lensDistortion(), lens.distortion(), "lens distortion");
                exact(params.focusDistance(), (float)state.focusDistance(), "focus distance");
                CameraOpticsParameters disabled = CameraOpticsParameters.fromCamera(state, 256.0F, false);
                exact(disabled.dofMultiplier(), 0.0F, "disabled lens DoF");
                exact(disabled.lensDistortion(), lens.distortion(), "distortion independent of DoF");
                if (disabled.needsOpticsPass() != (lens.distortion() != 0.0F)) {
                    throw new AssertionError("Optics gate changed for lens " + lens);
                }
            }
        }

        CameraOpticsParameters infinity = CameraOpticsParameters.fromCamera(
                original.withFocusDistance(128.0D).withFocalLength(200.0D), 1200.0F, true);
        exact(infinity.focusDistance(), 128.0F, "infinity focus");
        exact(infinity.focalLength(), 200.0F, "telephoto");
        exact(infinity.farPlane(), 1200.0F, "far draw distance");
        System.out.println("PASS: original optics uniforms for all five lenses, eight apertures, DoF and zoom");
    }
}
