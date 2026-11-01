package EdDYON.guaniao.fabric;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/** Real Fabric API compilation gate for the migrated Hummingbird loop callbacks. */
public final class GuaniaoHummingbirdSoundEventsProof {
    private GuaniaoHummingbirdSoundEventsProof() { }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> { });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> { });
    }
}
