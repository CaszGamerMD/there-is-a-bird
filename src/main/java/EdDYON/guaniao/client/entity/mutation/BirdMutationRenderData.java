package EdDYON.guaniao.client.entity.mutation;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/**
 * Snapshot the actual per-bird mutation texture before GeckoLib 5 releases
 * the live entity reference and starts its render pass.
 *
 * A render-state ticket avoids a global last-bird cache: birds with different
 * colors may render in the same frame without sharing one another's texture.
 */
public final class BirdMutationRenderData {
    public static final DataTicket<Identifier> MUTATION_TEXTURE =
            DataTicket.create("guaniao_mutation_texture", Identifier.class);

    private BirdMutationRenderData() { }

    public static void capture(GeoRenderState renderState, Identifier texture) {
        renderState.addGeckolibData(MUTATION_TEXTURE, texture);
    }

    public static Identifier texture(GeoRenderState renderState, Identifier fallback) {
        return renderState.getOrDefaultGeckolibData(MUTATION_TEXTURE, fallback);
    }
}
