package EdDYON.guaniao.client.camera;

/** Real standalone Java 25 regression for the original lens/calibration math. */
public final class GuaniaoCameraLensMathRegression {
    private GuaniaoCameraLensMathRegression() { }

    private static void near(double actual, double expected, String label) {
        if (!Double.isFinite(actual) || Math.abs(actual - expected) > 1.0E-10D) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) {
        // Every focal length must retain the original Nikon full-frame formula.
        for (double f : new double[]{12, 18, 35, 50, 75, 100, 150, 200}) {
            double authored = Math.min(
                    Math.toDegrees(2.0D * Math.atan(36.0D / (2.0D * f))), 110.0D);
            near(CameraLensMath.focalLengthToFov(f), authored, "fov " + f);
        }
        // Long lenses should not get clamped at 14 degrees.
        if (CameraLensMath.focalLengthToFov(150) >= 14.0D ||
                CameraLensMath.focalLengthToFov(200) >= 14.0D) {
            throw new AssertionError("Telephoto FOV was clamped");
        }
        // Minimum, halfway, max, and either side of the authored zoom range.
        near(CameraLensMath.focalSensitivityScale(18,18,200),1.0D,"wide sensitivity");
        near(CameraLensMath.focalSensitivityScale(200,18,200),0.28D,"tele sensitivity");
        near(CameraLensMath.focalSensitivityScale(109,18,200),0.64D,"mid sensitivity");
        near(CameraLensMath.focalSensitivityScale(-10,18,200),1.0D,"low clamp");
        near(CameraLensMath.focalSensitivityScale(1000,18,200),0.28D,"high clamp");
        double last = 180.0D;
        for (int f = 18; f <= 200; f += 2) {
            double current = CameraLensMath.focalLengthToFov(f);
            if (current > last) throw new AssertionError("FOV increased on zoom at " + f);
            last = current;
        }
        System.out.println("PASS: preserved 36 mm focal FOV, telephoto, sensitivity and zoom range");
    }
}
