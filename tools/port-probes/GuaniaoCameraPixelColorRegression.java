package EdDYON.guaniao.client.camera;

/**
 * Real JDK test for the 26.2 NativeImage ARGB to original camera ABGR seam.
 * The author filter and JPEG codec both expect alpha-blue-green-red byte order.
 */
public final class GuaniaoCameraPixelColorRegression {
    private GuaniaoCameraPixelColorRegression() { }

    private static void equal(int got, int expected, String label) {
        if (got != expected) {
            throw new AssertionError(label + ": " + Integer.toHexString(got)
                    + " != " + Integer.toHexString(expected));
        }
    }

    public static void main(String[] args) {
        equal(CameraPhotoSampler.argbToAbgr(0xFF123456), 0xFF563412, "RGB swap");
        equal(CameraPhotoSampler.argbToAbgr(0x00000000), 0x00000000, "transparent black");
        equal(CameraPhotoSampler.argbToAbgr(0x80112233), 0x80332211, "translucent pixel");
        equal(CameraPhotoSampler.argbToAbgr(0xFFFFFFFF), 0xFFFFFFFF, "white");
        equal(CameraPhotoSampler.argbToAbgr(0xFF00FF00), 0xFF00FF00, "green");
        // Byte exchange is its own inverse; all channels survive unchanged.
        for (int r = 0; r <= 255; r += 17) {
            for (int g = 0; g <= 255; g += 17) {
                for (int b = 0; b <= 255; b += 17) {
                    int argb = (0xAB << 24) | (r << 16) | (g << 8) | b;
                    equal(CameraPhotoSampler.argbToAbgr(CameraPhotoSampler.argbToAbgr(argb)),
                            argb, "round trip");
                }
            }
        }
        System.out.println("PASS: Minecraft 26.2 screenshot pixels retain original camera ABGR colors");
    }
}
