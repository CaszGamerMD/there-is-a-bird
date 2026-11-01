package EdDYON.guaniao.fabric;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.world.entity.Entity;

/** Compile-only test of 26.2 client-world tracking and Fabric sound lifecycle callbacks. */
public final class GuaniaoCockatooSoundLifecycleProof {
    private GuaniaoCockatooSoundLifecycleProof() { }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null) {
                for (Entity entity : client.level.entitiesForRendering()) {
                    entity.isAlive();
                }
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> { });
    }
}
