package EdDYON.guaniao.client.camera;

/** Actual physical framebuffer pixels (not GUI-scaled logical coordinates). */
record CameraFramebufferSize(int width, int height) {
    CameraFramebufferSize {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Camera framebuffer dimensions must be positive");
        }
    }

    static CameraFramebufferSize of(int width, int height) {
        return new CameraFramebufferSize(width, height);
    }

    boolean matches(int width, int height) {
        return this.width == width && this.height == height;
    }
}
