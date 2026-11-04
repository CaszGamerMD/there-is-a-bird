package EdDYON.guaniao.client.camera;

import EdDYON.guaniao.content.camera.CameraFilter;

/**
 * Original 51 authored filter selections, their stable IDs, and the same
 * CPU pipeline for saving JPEG photographs. Implementation is now pure Java
 * so effects can be regression-tested without a Minecraft renderer.
 */
final class CameraImageFilters {
    private CameraImageFilters() { }

    static void apply(int[] pixels, int width, int height, CameraFilter filter, long seed) {
        CameraFilterPixelCore.apply(
                pixels, width, height, filter.id(),
                filter == CameraFilter.COLOR_BALANCE,
                filter != CameraFilter.NONE,
                CameraFilter.GLITCH_RGB.id(),
                CameraFilter.CHROMATIC_ABERRATION.id(), seed);
    }
}
