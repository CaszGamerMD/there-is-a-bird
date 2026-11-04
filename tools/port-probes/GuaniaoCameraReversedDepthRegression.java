package EdDYON.guaniao.client.camera;

/**
 * Actual Java regression for the Nikon shader's 26.2 reversed-Z projection.
 * Minecraft 26.2 uses 0..1 reverse depth: near=1, far/clear sky=0.
 * This is CPU numerical parity coverage, not a GPU execution test.
 */
public final class GuaniaoCameraReversedDepthRegression {
    private GuaniaoCameraReversedDepthRegression() { }

    static double linearDepth(double reversedDepth, double near, double far) {
        double z = (1.0 - reversedDepth) * 2.0 - 1.0;
        return 2.0 * near * far
                / Math.max(0.0001, far + near - z * (far - near));
    }

    static double project(double distance, double near, double far) {
        return near * (far - distance) / (distance * (far - near));
    }

    static boolean sky(double reversedDepth) {
        return reversedDepth <= 0.000001;
    }

    private static void near(double actual, double expected, double tolerance, String label) {
        if (!Double.isFinite(actual) || Math.abs(actual - expected) > tolerance) {
            throw new AssertionError(label + ": " + actual + " != " + expected);
        }
    }

    public static void main(String[] args) {
        for (double far : new double[]{32.0, 128.0, 512.0, 1200.0}) {
            double near = 0.05;
            near(linearDepth(1.0, near, far), near, 1.0E-8, "near plane");
            near(linearDepth(0.0, near, far), far, 0.01, "far plane");
            if (!sky(0.0) || !sky(0.0000005) || sky(0.01) || sky(1.0))
                throw new AssertionError("Reversed clear-depth sky test");
            double prev = Double.POSITIVE_INFINITY;
            for (int i = 0; i <= 100; i++) {
                double d = i / 100.0;
                double distance = linearDepth(d, near, far);
                if (distance > prev + 0.00001)
                    throw new AssertionError("Reversed depth must decrease with buffer depth");
                prev = distance;
            }
            for (double distance : new double[]{near, 0.25, 0.5, 1.0, 2.4, 12.0, 24.0, 128.0, far}) {
                if (distance > far) continue;
                double reverse = project(distance, near, far);
                near(linearDepth(reverse, near, far), distance, 1.0E-5, "round trip " + distance);
            }
        }
        System.out.println("PASS: reversed-Z near/far/sky and camera focus distance reconstruction");
    }
}
