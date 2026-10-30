package EdDYON.guaniao.client.camera;

import java.util.Arrays;

/** Actual standalone Java regression across all original 51 camera effects. */
public final class GuaniaoCameraFilterCoreRegression {
    private GuaniaoCameraFilterCoreRegression() { }

    private static int[] sample(int width, int height) {
        int[] result = new int[width * height];
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            int red = (x * 37 + y * 11) & 255;
            int green = (x * 13 + y * 29) & 255;
            int blue = (x * 19 + y * 7) & 255;
            int alpha = (x * 23 + y * 31) & 255;
            result[y * width + x] = (alpha << 24) | (blue << 16) | (green << 8) | red;
        }
        return result;
    }

    private static void effect(int[] pixels, int w, int h, int id) {
        CameraFilterPixelCore.apply(pixels, w, h, id, id == 4, id != 0, 44, 45, 123456789L);
    }

    public static void main(String[] args) {
        for (int width : new int[]{1, 2, 9, 16}) {
            for (int height : new int[]{1, 2, 9, 11}) {
                int[] source = sample(width, height);
                for (int id = 0; id <= 50; id++) {
                    int[] first = source.clone();
                    int[] second = source.clone();
                    effect(first, width, height, id);
                    effect(second, width, height, id);
                    if (!Arrays.equals(first, second)) {
                        throw new AssertionError("Nondeterministic effect: " + id);
                    }
                    for (int i = 0; i < first.length; i++) {
                        if ((first[i] & 0xFF000000) != (source[i] & 0xFF000000)) {
                            throw new AssertionError("Effect " + id + " altered alpha at " + i);
                        }
                        if (id == 1) {
                            int red=first[i] & 255;
                            int green=(first[i] >>> 8) & 255;
                            int blue=(first[i] >>> 16) & 255;
                            if (red != green || red != blue) {
                                throw new AssertionError("Black/white is not neutral at " + i);
                            }
                        }
                    }
                    // The original NONE effect has no sharpening on 1x1 tiles.
                    if (id == 0 && width == 1 && height == 1 && first[0] != source[0]) {
                        throw new AssertionError("NONE changed a single pixel");
                    }
                }
            }
        }
        System.out.println("PASS: all 51 original CPU filters preserve alpha, deterministic seed and B/W");
    }
}
