package EdDYON.guaniao.client.camera;

import EdDYON.guaniao.content.camera.CameraFilter;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Camera GUI extraction boundary: only an actual filtered frame or thumbnail
 * may be submitted to 26.2's deferred GUI render state. Never substitute an
 * unfiltered screenshot for a real author filter preview.
 *
 * The legacy OpenGL CameraPreviewPostEffect remains available for source
 * parity but cannot draw into GuiGraphicsExtractor.
 */
public final class CameraPreviewGuiBridge {
    public interface Renderer {
        /**
         * @return true only if the selected filter's real world preview
         *         was submitted to the GUI extraction render state.
         */
        boolean drawPreview(GuiGraphicsExtractor graphics, int left, int top,
                            int right, int bottom, CameraFilter filter);

        /** @return true only if a real filtered thumbnail was submitted. */
        boolean drawThumbnail(GuiGraphicsExtractor graphics, CameraFilter filter,
                              int left, int top, int right, int bottom);
    }

    private static Renderer renderer;

    private CameraPreviewGuiBridge() { }

    /** Install only after the native 26.2 preview renderer is initialized. */
    public static void install(Renderer replacement) {
        renderer = Objects.requireNonNull(replacement, "renderer");
    }

    /** Release stale renderer resources after client disconnect/reload. */
    public static void clear() {
        renderer = null;
    }

    static boolean drawPreview(GuiGraphicsExtractor graphics, int left, int top,
                               int right, int bottom, CameraFilter filter) {
        Renderer active = renderer;
        return active != null && filter != null
                && right > left && bottom > top
                && active.drawPreview(graphics, left, top, right, bottom, filter);
    }

    static boolean drawFilterThumbnail(GuiGraphicsExtractor graphics, CameraFilter filter,
                                       int left, int top, int right, int bottom) {
        Renderer active = renderer;
        return active != null && filter != null
                && right > left && bottom > top
                && active.drawThumbnail(graphics, filter, left, top, right, bottom);
    }
}
