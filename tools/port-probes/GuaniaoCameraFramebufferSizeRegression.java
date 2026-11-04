package EdDYON.guaniao.client.camera;

/** Standalone Java test of true framebuffer dimensions and recreation guards. */
public final class GuaniaoCameraFramebufferSizeRegression {
    private GuaniaoCameraFramebufferSizeRegression() { }

    private static void valid(int width, int height) {
        CameraFramebufferSize actual = CameraFramebufferSize.of(width, height);
        if (!actual.matches(width, height)) throw new AssertionError("Same-size frame rejected");
        if (actual.matches(width + 1, height) || actual.matches(width, height + 1))
            throw new AssertionError("Resize was not detected");
        if (!actual.equals(new CameraFramebufferSize(width, height)))
            throw new AssertionError("Same-size record identity mismatch");
    }

    private static void invalid(int width, int height) {
        try {
            CameraFramebufferSize.of(width, height);
            throw new AssertionError("Invalid framebuffer accepted: " + width + "x" + height);
        } catch (IllegalArgumentException expected) {
            // No zero/negative GPU attachments must ever be created.
        }
    }

    public static void main(String[] args) {
        valid(1, 1);
        valid(1024, 1024);
        valid(1920, 1080);
        valid(2560, 1440);
        valid(3840, 2160);
        valid(7680, 4320);
        invalid(0, 1080);
        invalid(1920, 0);
        invalid(-1, 1);
        invalid(1, -1);
        System.out.println("PASS: camera framebuffer stays pixel-accurate and resizes on either dimension");
    }
}
