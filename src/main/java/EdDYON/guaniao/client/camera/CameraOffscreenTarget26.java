package EdDYON.guaniao.client.camera;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;

/**
 * A real, Vulkan-compatible off-screen copy of the current camera world frame.
 *
 * IMPORTANT: This is only the INPUT to the original Nikon GPU filters. The
 * eventual 26.2 renderer must run CameraPostChain26 on this private target and
 * submit its true filtered color texture through CameraPreviewGuiBridge.
 *
 * Never process minecraft.gameRenderer.mainRenderTarget() in-place: that would
 * filter the entire player's screen, interfere with the HUD and change the
 * clean JPEG shutter. Never expose a copy as a filtered preview.
 */
public final class CameraOffscreenTarget26 implements AutoCloseable {
    private TextureTarget target;
    private CameraFramebufferSize size;

    /**
     * Snapshot the actual world color and reversed-Z depth to an owned texture.
     * Missing source attachments fail closed instead of publishing a fake lens.
     * Must be called in the GPU draw phase, not GUI extraction.
     */
    public RenderTarget copyWorld(RenderTarget world) {
        Objects.requireNonNull(world, "world");
        RenderSystem.assertOnRenderThread();
        if (world.width <= 0 || world.height <= 0
                || world.getColorTexture() == null || world.getDepthTexture() == null) {
            return null;
        }

        CameraFramebufferSize next = CameraFramebufferSize.of(world.width, world.height);
        if (!next.equals(this.size) || target == null) {
            close();
            // The target stores physical pixels, not Minecraft's scaled GUI.
            // The native RenderTarget methods use Blaze3D on OpenGL AND Vulkan.
            this.target = new TextureTarget(
                    "guaniao-camera-world-copy", next.width(), next.height(),
                    true, GpuFormat.RGBA8_UNORM);
            this.size = next;
        }
        if (target.getColorTexture() == null || target.getDepthTexture() == null) {
            // A failed GPU allocation must not expose an incomplete preview.
            close();
            return null;
        }
        // Minecraft 26.2 has no RenderTarget.copyColorFrom. Copy directly via
        // the cross-backend GPU command encoder, matching vanilla's depth-copy
        // path. The visible main target is only a source; it is never modified.
        RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
                world.getColorTexture(), target.getColorTexture(),
                0, 0, 0, 0, 0, next.width(), next.height());
        target.copyDepthFrom(world);
        return target;
    }

    /** For resize/reload/disconnect handlers; closes GPU attachments exactly once. */
    @Override
    public void close() {
        if (target != null) {
            target.destroyBuffers();
            target = null;
        }
        size = null;
    }

    public boolean hasMatchingFrame(int width, int height) {
        return target != null && size != null && size.matches(width, height);
    }
}
