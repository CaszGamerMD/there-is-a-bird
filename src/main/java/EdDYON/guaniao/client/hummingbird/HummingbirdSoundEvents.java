package EdDYON.guaniao.client.hummingbird;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/** Registers the client sound loop lifecycle with Fabric rather than Forge's event bus. */
public final class HummingbirdSoundEvents {
    private HummingbirdSoundEvents() { }

    public static void registerCallbacks() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> HummingbirdSoundManager.INSTANCE.tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> HummingbirdSoundManager.INSTANCE.clear());
        // The manager also detects a changed/unloaded client level every tick
        // and retires inactive playback after a sound-engine restart.
    }
}
