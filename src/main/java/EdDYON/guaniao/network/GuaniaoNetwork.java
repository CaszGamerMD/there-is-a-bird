package EdDYON.guaniao.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Compatibility adapter for any remaining server-side callers.
 * The actual 26.2 packet registrations now live in
 * EdDYON.guaniao.fabric.GuaniaoFabricNetworking.
 *
 * Client senders use ClientPlayNetworking directly on the client side.
 */
@Deprecated(forRemoval = true)
public final class GuaniaoNetwork {
    private GuaniaoNetwork() { }

    public static void sendToPlayer(CustomPacketPayload packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, packet);
    }
}
