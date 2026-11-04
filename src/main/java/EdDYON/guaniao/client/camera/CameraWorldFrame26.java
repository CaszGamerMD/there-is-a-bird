package EdDYON.guaniao.client.camera;

import EdDYON.guaniao.GuaniaoMod;
import EdDYON.guaniao.content.camera.CameraFilter;
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
            // The original authored GPU shader runs only on the private
            // copy. NEVER process the visible source RenderTarget.
            CameraFilter filter = CameraClientCapture.currentFilter();
            if (updated == null
                    || !CameraPostChain26.processFilterOnPrivate(minecraft, filter, updated)) {
                copiedWorld = null;
                return;
            }
            copiedWorld = updated;
            warned = false;
        } catch (RuntimeException exception) {
            reset();
            if (!warned) {
                GuaniaoMod.LOGGER.warn("Cannot copy Nikon camera world framebuffer", exception);
                warned = true;
            }
        }
    }

    /**
     * The native GUI renderer may read this PRIVATE filtered world frame.
     * Null means there is no authentic completed filter for this world frame.
     * Nikon optics and live GPU Time uniform updates remain a separate stage;
     * do not claim a complete preview until those are also wired.
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
