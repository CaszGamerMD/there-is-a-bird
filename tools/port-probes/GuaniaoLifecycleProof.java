package EdDYON.guaniao.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/**
 * Compile-only 26.2 event signature probe. Never call this method:
 * these are intentionally no-op callbacks and not part of the final mod.
 */
public final class GuaniaoLifecycleProof {
    private GuaniaoLifecycleProof() { }

    public static void probe() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> { });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> { });
        ServerLevelEvents.UNLOAD.register((server, level) -> { });
        ServerTickEvents.END_SERVER_TICK.register(server -> { });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> { });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> { });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> { });
        EntityTrackingEvents.START_TRACKING.register((target, player) -> { });
        EntityTrackingEvents.STOP_TRACKING.register((target, player) -> { });
    }
}
