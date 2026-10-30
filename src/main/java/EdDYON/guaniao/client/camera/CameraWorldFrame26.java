package EdDYON.guaniao.client.camera;

import EdDYON.guaniao.GuaniaoMod;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;

/**
 * First native 26.2 drawing-phase handoff for real camera world pixels.
 *
 * END_MAIN runs after the main world terrain/entities/particles have been
 * submitted to its targets, before clouds/weather and final UI composition.
 * This is a PREVIEW INPUT stage, not the final clean JPEG shutter or a
 * substitute for the original fully composed filtered camera view.
 *
 * No copying occurs until a genuine 26.2 filtered preview renderer has been
 * installed. This prevents a costly framebuffer copy on every ordinary frame.
 */
public final class CameraWorldFrame26 {
    private static final CameraOffscreenTarget26 OFFSCREEN = new CameraOffscreenTarget26();
    private static RenderTarget copiedWorld;
    private static boolean warned;

    private CameraWorldFrame26() { }

    public static void register() {
        LevelRenderEvents.END_MAIN.register(context -> captureWorld());
    }

    private static void captureWorld() {
        if (!CameraClientCapture.isViewfinderOpen()
                || CameraClientCapture.isCleanCapturePending()
                || !CameraPreviewGuiBridge.hasRenderer()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;
        try {
            RenderTarget source = minecraft.gameRenderer.mainRenderTarget();
            RenderTarget updated = OFFSCREEN.copyWorld(source);
            copiedWorld = updated;
            if (updated != null) warned = false;
        } catch (RuntimeException exception) {
            reset();
            if (!warned) {
                GuaniaoMod.LOGGER.warn("Cannot copy Nikon camera world framebuffer", exception);
                warned = true;
            }
        }
    }

    /**
     * The real GPU post-chain renderer may use this PRIVATE copy as input.
     * Null means no current world frame is available or the camera was closed.
     * Do not expose this unfiltered color texture to the viewfinder as a
     * 'successful' filter or photograph.
     */
    public static RenderTarget copiedWorld() {
        return copiedWorld;
    }

    /** Called on camera close, clean shutter and server disconnect. */
    public static void reset() {
        copiedWorld = null;
        OFFSCREEN.close();
    }
}
