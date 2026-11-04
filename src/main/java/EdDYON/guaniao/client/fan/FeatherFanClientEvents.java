package EdDYON.guaniao.client.fan;

import EdDYON.guaniao.content.fan.FeatherFanItem;
import EdDYON.guaniao.network.FeatherFanPiercePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;

/** The Fabric client attack callback replaces the Forge mouse-button hook. */
public final class FeatherFanClientEvents {
    private FeatherFanClientEvents() { }

    public static void registerCallbacks() {
        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
            // The Fabric callback also fires while the attack key is held.
            // Trigger only on a fresh click, matching the original GLFW press.
            if (clickCount == 0 || client.screen != null || !FeatherFanItem.isFullyCharged(player)) {
                return false;
            }
            if (ClientPlayNetworking.canSend(FeatherFanPiercePacket.TYPE)) {
                ClientPlayNetworking.send(new FeatherFanPiercePacket());
            }
            // A charged fan attack cannot accidentally mine or hit an entity.
            return true;
        });
    }
}
