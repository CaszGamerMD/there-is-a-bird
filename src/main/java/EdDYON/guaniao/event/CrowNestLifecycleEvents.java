package EdDYON.guaniao.event;

import EdDYON.guaniao.content.nest.CrowNestBlockEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.server.level.ServerLevel;

/** Clear transient nest tracking when a server world unloads. */
public final class CrowNestLifecycleEvents {
    private CrowNestLifecycleEvents() { }

    public static void onLevelUnload(ServerLevel level) {
        CrowNestBlockEntity.clearLoadedNests(level);
    }

    public static void registerCallbacks() {
        ServerLevelEvents.UNLOAD.register((server, level) -> onLevelUnload(level));
    }
}
