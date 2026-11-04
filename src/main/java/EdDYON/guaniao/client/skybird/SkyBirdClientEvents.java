package EdDYON.guaniao.client.skybird;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

/** Fabric client tick, extraction and deferred world draw callbacks for sky ecology. */
public final class SkyBirdClientEvents {
    private SkyBirdClientEvents() {
    }

    public static void registerCallbacks() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> SkyBirdManager.INSTANCE.tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            SkyBirdManager.INSTANCE.clear();
            SkyBirdRenderer.clear();
        });
        LevelExtractionEvents.END_EXTRACTION.register(
                context -> SkyBirdRenderer.extract(context, SkyBirdManager.INSTANCE));
        // Render after terrain but before clouds and weather, using 26.2's deferred
        // collector and an immutable snapshot prepared above.
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(SkyBirdRenderer::submit);
    }
}
