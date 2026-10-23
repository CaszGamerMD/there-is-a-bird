package EdDYON.guaniao.event;

import EdDYON.guaniao.config.BirdConfigManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Keep the original server-scoped configuration lifecycle. The global
 * settings are restored when the world shuts down.
 */
public final class BirdConfigLifecycleEvents {
    private BirdConfigLifecycleEvents() { }

    public static void onServerAboutToStart(MinecraftServer server) {
        BirdConfigManager.loadForServer(server);
    }

    public static void onServerStopping(MinecraftServer server) {
        BirdConfigManager.loadOrCreateDefaults();
    }

    public static void registerCallbacks() {
        ServerLifecycleEvents.SERVER_STARTING.register(BirdConfigLifecycleEvents::onServerAboutToStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(BirdConfigLifecycleEvents::onServerStopping);
    }
}
