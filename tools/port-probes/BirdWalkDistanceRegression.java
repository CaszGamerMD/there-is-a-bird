package EdDYON.guaniao.content.bird;

/**
 * Executable compile-isolated regression checks for original distance-driven
 * bird gait semantics. This is not shipped inside the mod JAR.
 */
public final class BirdWalkDistanceRegression {
    private BirdWalkDistanceRegression() { }

    private static void close(double expected, double actual, String label) {
        if (!Double.isFinite(actual) || Math.abs(expected - actual) > 1.0E-8) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) {
        BirdWalkDistance walking = new BirdWalkDistance();
        walking.sample(0, 0, 0, true, 1, 1);
        walking.sample(1, 0.25, 0, true, 1, 1);
        close(1, walking.pixels(), "per-tick gait cap");
        walking.sample(1, 42, 0, true, 1, 1);
        close(1, walking.pixels(), "duplicate render timestamp");
        walking.sample(2, 0.5, 0, true, 1, 1);
        close(2, walking.pixels(), "next movement tick");
        walking.sample(3, 0.5, 0, true, 1, 1);
        close(2, walking.pixels(), "stop immediately without gait backlog");
        walking.sample(4, 0.6, 0, false, 1, 10);
        walking.sample(5, 0.7, 0, true, 1, 10);
        close(2, walking.pixels(), "landing must not replay air travel");
        walking.sample(6, 0.8, 0, true, 1, 10);
        close(3.6, walking.pixels(), "grounded motion resumes");
        walking.sample(7, 10, 0, true, 1, 10);
        close(3.6, walking.pixels(), "teleport rejection");
        walking.sample(13, 10.2, 0, true, 1, 10);
        close(3.6, walking.pixels(), "inactive time gap rejection");

        BirdWalkDistance scaled = new BirdWalkDistance();
        scaled.sample(0, 0, 0, true, 2);
        scaled.sample(1, 0.25, 0, true, 2);
        close(2, scaled.pixels(), "individual model render scale");
        scaled.sample(2, 0.5, 0, true, Double.NaN);
        close(2, scaled.pixels(), "invalid model scale rejection");

        System.out.println("PASS: distance-driven bird gait baseline, cap, ground state, scale and teleport checks");
    }
}
