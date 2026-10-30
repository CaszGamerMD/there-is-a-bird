package EdDYON.guaniao.client.camera;

/**
 * The original Nikon 36 mm full-frame lens math, independent of Forge events
 * and the game renderer. Both live viewfinder FOV and clean captured frames
 * use this exact formula. This also preserves the original smooth mouse
 * sensitivity curve over the supported focal range.
 */
public final class CameraLensMath {
    private static final double FULL_FRAME_SENSOR_WIDTH = 36.0D;
    private static final double MIN_VIEWFINDER_SENSITIVITY_SCALE = 0.28D;

    private CameraLensMath() { }

    public static double focalLengthToFov(double focalLength) {
        double fov = Math.toDegrees(2.0D * Math.atan(FULL_FRAME_SENSOR_WIDTH / (2.0D * focalLength)));
        // Minecraft uses vertical FOV: allow the original telephoto range.
        return Math.min(fov, 110.0D);
    }

    public static double focalSensitivityScale(double focalLength, double minimum, double maximum) {
        // Same clamp and lerp as the original Mth-based 1.20.1 implementation.
        double normalized = Math.max(0.0D, Math.min(1.0D,
                (focalLength - minimum) / (maximum - minimum)));
        double smooth = normalized * normalized * (3.0D - 2.0D * normalized);
        return 1.0D + smooth * (MIN_VIEWFINDER_SENSITIVITY_SCALE - 1.0D);
    }
}
