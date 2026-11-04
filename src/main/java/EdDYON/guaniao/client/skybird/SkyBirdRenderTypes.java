package EdDYON.guaniao.client.skybird;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Texture-bound 26.2 pipeline for sky sprites. The vanilla emissive
 * translucent entity setup blends alpha, disables depth writes and culling,
 * and therefore retains the original sky layer's non-occluding two-sided look.
 */
final class SkyBirdRenderTypes {
    private SkyBirdRenderTypes() {
    }

    static RenderType translucent(Identifier texture) {
        return RenderTypes.entityTranslucentEmissive(texture, false);
    }
}
