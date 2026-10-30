package EdDYON.guaniao.client.camera;

/**
 * The original viewfinder-to-JPEG crop, independent of native GPU screenshot APIs.
 * Reads the exact same source ABGR pixels in their original order; the author
 * photo filter and JPEG compression continue after sampling, unchanged.
 */
final class CameraPhotoSampler {
    @FunctionalInterface
    interface PixelReader {
        int get(int x, int y);
    }

    private CameraPhotoSampler() { }

    static int[] cropSquare(int guiWidth, int guiHeight, int fontLineHeight,
                            int sourceWidth, int sourceHeight, int size,
                            PixelReader source) {
        if (guiWidth <= 0 || guiHeight <= 0 || sourceWidth <= 0 || sourceHeight <= 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid camera capture dimensions");
        }
        java.util.Objects.requireNonNull(source, "source");
        CameraViewfinderFrame.Pixels crop = CameraViewfinderFrame.layout(
                guiWidth, guiHeight, fontLineHeight).pixels(
                guiWidth, guiHeight, sourceWidth, sourceHeight);
        int[] pixels = new int[Math.multiplyExact(size, size)];
        for (int y = 0; y < size; y++) {
            int sampleY = crop.top() + Math.min(crop.height() - 1, (int)((y + 0.5D) * crop.height() / size));
            for (int x = 0; x < size; x++) {
                int sampleX = crop.left() + Math.min(crop.width() - 1, (int)((x + 0.5D) * crop.width() / size));
                pixels[y * size + x] = source.get(sampleX, sampleY);
            }
        }
        return pixels;
    }
}
