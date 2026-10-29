package EdDYON.guaniao.fabric;

import EdDYON.guaniao.client.entity.budgerigar.BudgerigarRenderer;
import EdDYON.guaniao.client.entity.kiwi.KiwiRenderer;
import EdDYON.guaniao.client.entity.longtailedtit.LongTailedTitRenderer;
import EdDYON.guaniao.client.entity.macaw.MacawRenderer;
import EdDYON.guaniao.client.entity.myna.MynaRenderer;
import EdDYON.guaniao.client.entity.sparrow.SparrowRenderer;
import EdDYON.guaniao.registry.GuaniaoEntityTypes;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * First group of GeckoLib 5-compatible bird renderers.
 * Keep this entirely client-side; unported renderer/layer classes are
 * intentionally not loaded until their render-state conversions are complete.
 */
public final class GuaniaoFabricEntityRenderers {
    private GuaniaoFabricEntityRenderers() { }

    public static void register() {
        EntityRendererRegistry.register(GuaniaoEntityTypes.KIWI.get(), KiwiRenderer::new);
        EntityRendererRegistry.register(GuaniaoEntityTypes.SPARROW.get(), SparrowRenderer::new);
        EntityRendererRegistry.register(GuaniaoEntityTypes.MYNA.get(), MynaRenderer::new);
        EntityRendererRegistry.register(GuaniaoEntityTypes.LONG_TAILED_TIT.get(), LongTailedTitRenderer::new);
        EntityRendererRegistry.register(GuaniaoEntityTypes.MACAW.get(), MacawRenderer::new);
        EntityRendererRegistry.register(GuaniaoEntityTypes.BUDGERIGAR.get(), BudgerigarRenderer::new);
    }
}
