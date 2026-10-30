import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

/**
 * Compile-only 26.2 API proof for the Frame Workbench 3D preview path.
 * This probe is NEVER registered by the mod; the real custom mesh,
 * photo texture and GeckoLib bird pipeline must be ported separately.
 */
public final class GuaniaoFramePictureInPictureProof {
    private GuaniaoFramePictureInPictureProof() { }

    public record State(int x0, int y0, int x1, int y1, float scale,
                        ScreenRectangle scissorArea, ScreenRectangle bounds)
            implements PictureInPictureRenderState {
        public State(int x0, int y0, int x1, int y1, float scale, ScreenRectangle scissorArea) {
            this(x0, y0, x1, y1, scale, scissorArea,
                    PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
        }
    }

    public static final class Renderer extends PictureInPictureRenderer<State> {
        @Override
        public Class<State> getRenderStateClass() {
            return State.class;
        }

        @Override
        protected String getTextureLabel() {
            return "guaniao 26.2 api verification only";
        }

        @Override
        protected void renderToTexture(State state, PoseStack pose, SubmitNodeCollector collector) {
            // Prove the new queued-geometry submission, vertex transforms and
            // 3D depth target API together. This does not draw in the live mod.
            collector.submitCustomGeometry(pose, RenderTypes.lines(), (snapshot, vertices) -> {
                vertices.addVertex(snapshot, -0.5F, 0.0F, 0.0F).setColor(-1);
                vertices.addVertex(snapshot, 0.5F, 0.0F, 0.0F).setColor(-1);
            });
        }
    }

    public static void registerProofOnly() {
        PictureInPictureRendererRegistry.register(context -> new Renderer());
    }
}
