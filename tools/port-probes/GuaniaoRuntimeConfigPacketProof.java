package EdDYON.guaniao.fabric;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Real Fabric 26.2 compilation gate for a three-boolean clientbound payload. */
public final class GuaniaoRuntimeConfigPacketProof {
    public record ThreeBooleans(boolean a, boolean b, boolean c) implements CustomPacketPayload {
        public static final Type<ThreeBooleans> TYPE = new Type<>(
                Identifier.fromNamespaceAndPath("guaniao", "runtime_config_proof"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ThreeBooleans> CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, ThreeBooleans::a,
                        ByteBufCodecs.BOOL, ThreeBooleans::b,
                        ByteBufCodecs.BOOL, ThreeBooleans::c, ThreeBooleans::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register(ThreeBooleans snapshot, net.minecraft.server.level.ServerPlayer player) {
        PayloadTypeRegistry.clientboundPlay().register(ThreeBooleans.TYPE, ThreeBooleans.CODEC);
        if (ServerPlayNetworking.canSend(player, ThreeBooleans.TYPE)) {
            ServerPlayNetworking.send(player, snapshot);
        }
    }
}
