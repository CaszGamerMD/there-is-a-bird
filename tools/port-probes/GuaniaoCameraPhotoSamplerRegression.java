package EdDYON.guaniao.client.camera;

import java.util.concurrent.atomic.AtomicInteger;

/** Real standalone, pixel-level regression against the original camera crop. */
public final class GuaniaoCameraPhotoSamplerRegression {
    private GuaniaoCameraPhotoSamplerRegression() { }

    private static void equal(int actual, int expected, String label) {
        if (actual != expected) throw new AssertionError(label + ": " + actual + " != " + expected);
    }

    private static void exactScale(int sourceWidth, int sourceHeight, int guiWidth, int guiHeight,
                                   int size, int firstX, int firstY, int spacing) {
        var calls = new AtomicInteger();
        int[] actual = CameraPhotoSampler.cropSquare(guiWidth, guiHeight, 9, sourceWidth, sourceHeight,
                size, (x, y) -> {
                    if (x < 0 || x >= sourceWidth || y < 0 || y >= sourceHeight)
                        throw new AssertionError("Out of framebuffer");
                    calls.incrementAndGet();
                    return (y << 16) | x;
                });
        equal(actual.length, size * size, "photo pixels");
        equal(calls.get(), size * size, "source reads");
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++)
            equal(actual[y * size + x], ((firstY + y * spacing) << 16) | (firstX + x * spacing),
                    "pixel " + x + "," + y);
    }

    public static void main(String[] args) {
        // 100x100 GUI at 9 px font => original 28 px lens, (36,36).
        exactScale(100,100,100,100,28,36,36,1);
        // DPI 2:1 maps to the same eyepiece at physical 56 px.
        exactScale(200,200,100,100,28,73,73,2);
        // Non-square GUI and scaled framebuffer use the original crop center.
        exactScale(240,200,120,100,28,93,73,2);
        // Downsampling reads the physical center of each tile.
        exactScale(100,100,100,100,14,37,37,2);
        try {
            CameraPhotoSampler.cropSquare(0,100,9,100,100,28,(x,y)->0);
            throw new AssertionError("Invalid GUI size accepted");
        } catch (IllegalArgumentException expected) { }
        try {
            CameraPhotoSampler.cropSquare(100,100,9,100,100,0,(x,y)->0);
            throw new AssertionError("Invalid output size accepted");
        } catch (IllegalArgumentException expected) { }
        System.out.println("PASS: original camera photo crop matches frame at 1x/2x DPI and non-square GUI");
    }
}
