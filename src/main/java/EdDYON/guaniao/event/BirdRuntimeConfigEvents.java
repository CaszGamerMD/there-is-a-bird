package EdDYON.guaniao.event;

import EdDYON.guaniao.config.BirdConfigManager;
import EdDYON.guaniao.network.BirdRuntimeConfigPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

/**
 * Keep the original per-player bird runtime configuration synchronization.
 * The old Forge login event becomes Fabric's server play JOIN callback, when
 * the player is ready to receive packets.
 */
public final class BirdRuntimeConfigEvents {
    private BirdRuntimeConfigEvents() { }

    public static void onPlayerLogin(ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, BirdRuntimeConfigPacket.TYPE)) {
            ServerPlayNetworking.send(player, new BirdRuntimeConfigPacket(
                    BirdConfigManager.birdsPassThroughLeaves(),
                    BirdConfigManager.aprilFoolsMode(),
                    BirdConfigManager.skyBirdEcologyEnabled()));
        }
    }

    public static void registerCallbacks() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                onPlayerLogin(handler.getPlayer()));
    }
}
