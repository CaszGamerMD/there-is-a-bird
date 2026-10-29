package EdDYON.guaniao.fabric;

import EdDYON.guaniao.network.FeatherFanPiercePacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Incremental native 26.2 networking registration.
 * The remaining original photo/config packet channels are not yet migrated.
 */
public final class GuaniaoFabricNetworking {
    private GuaniaoFabricNetworking() { }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(
                FeatherFanPiercePacket.TYPE, FeatherFanPiercePacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                FeatherFanPiercePacket.TYPE, (packet, context) -> packet.handle(context.player()));
    }
}
