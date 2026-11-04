package EdDYON.guaniao.network;

import EdDYON.guaniao.GuaniaoMod;
import EdDYON.guaniao.config.BirdConfigData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;


public final class OpenBirdConfigPacket implements CustomPacketPayload {
    public static final Type<OpenBirdConfigPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "open_bird_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBirdConfigPacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, OpenBirdConfigPacket packet) ->
                    encode(packet, buffer), OpenBirdConfigPacket::decode);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public BirdConfigData data() { return data.copy(); }

    private final BirdConfigData data;

    public OpenBirdConfigPacket(BirdConfigData data) {
        this.data = data.copy();
    }

    public static void encode(OpenBirdConfigPacket packet, FriendlyByteBuf buffer) {
        BirdConfigPacketCodec.encode(buffer, packet.data);
    }

    public static OpenBirdConfigPacket decode(FriendlyByteBuf buffer) {
        return new OpenBirdConfigPacket(BirdConfigPacketCodec.decode(buffer));
    }

}
