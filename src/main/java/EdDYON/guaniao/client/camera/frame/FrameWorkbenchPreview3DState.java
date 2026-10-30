package EdDYON.guaniao.client.camera.frame;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jspecify.annotations.Nullable;

/**
 * Immutable snapshot of the authored frame's true silhouette, surface
 * textures and preview rotation. Consumed only after the GUI extraction
 * pass, never by an immediate-mode GUI renderer.
 */
public record FrameWorkbenchPreview3DState(
        FrameWorkbenchMeshGeometry.Mesh mesh,
        Surface wood, Surface trim, Surface lining,
        float yaw, float pitch,
        int x0, int y0, int x1, int y1, float scale,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {
    public FrameWorkbenchPreview3DState(
            FrameWorkbenchMeshGeometry.Mesh mesh, Surface wood, Surface trim, Surface lining,
            float yaw, float pitch, int x0, int y0, int x1, int y1, float scale,
            @Nullable ScreenRectangle scissorArea) {
        this(mesh, wood, trim, lining, yaw, pitch,
                x0, y0, x1, y1, scale, scissorArea,
                PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }

    public record Surface(TextureAtlasSprite sprite, float red, float green,
                          float blue, boolean translucent) {
    }
}
