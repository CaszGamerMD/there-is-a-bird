package EdDYON.guaniao.fabric;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Isolated real-Fabric 26.2 compile proof for an empty client-to-server payload. */
public final class GuaniaoFeatherFanPacketProof {
    public record Ping() implements CustomPacketPayload {
        public static final Type<Ping> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("guaniao", "fan_proof"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Ping> CODEC =
                StreamCodec.unit(new Ping());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private GuaniaoFeatherFanPacketProof() { }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(Ping.TYPE, Ping.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Ping.TYPE, (packet, context) -> {
            // Handler always runs on the server thread.
            context.player().getUseItem();
        });
    }
}
