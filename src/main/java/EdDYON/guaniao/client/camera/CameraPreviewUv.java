package EdDYON.guaniao.client.camera;

/**
 * Original camera preview texture coordinates, independent of Minecraft's
 * renderer. Preserves GUI lens alignment, vertical framebuffer inversion and
 * the center-crop policy for preview thumbnails on non-square screens.
 */
final class CameraPreviewUv {
    record Uv(float u0, float vBottom, float u1, float vTop) { }

    private CameraPreviewUv() { }

    static Uv lens(int left, int top, int right, int bottom, int guiWidth, int guiHeight) {
        if (guiWidth <= 0 || guiHeight <= 0 || right <= left || bottom <= top)
            throw new IllegalArgumentException("Invalid camera lens rectangle");
        float u0 = (float)left / guiWidth;
        float u1 = (float)right / guiWidth;
        float vTop = 1.0F - (float)top / guiHeight;
        float vBottom = 1.0F - (float)bottom / guiHeight;
        return new Uv(u0, vBottom, u1, vTop);
    }

    static Uv centerCrop(int sourceWidth, int sourceHeight, int width, int height) {
        if (sourceWidth <= 0 || sourceHeight <= 0 || width <= 0 || height <= 0)
            throw new IllegalArgumentException("Invalid camera preview dimensions");
        float sourceAspect = sourceWidth / (float)Math.max(1, sourceHeight);
        float destinationAspect = width / (float)Math.max(1, height);
        float u0 = 0.0F;
        float u1 = 1.0F;
        float vBottom = 0.0F;
        float vTop = 1.0F;

        if (sourceAspect > destinationAspect) {
            float visibleWidth = destinationAspect / sourceAspect;
            float crop = (1.0F - visibleWidth) * 0.5F;
            u0 = crop;
            u1 = 1.0F - crop;
        } else if (sourceAspect < destinationAspect) {
            float visibleHeight = sourceAspect / destinationAspect;
            float crop = (1.0F - visibleHeight) * 0.5F;
            vBottom = crop;
            vTop = 1.0F - crop;
        }

        return new Uv(u0, vBottom, u1, vTop);
    }
}
