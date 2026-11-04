package EdDYON.guaniao.network;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PhotoCaptureResultPacket(UUID uploadId, boolean success) implements CustomPacketPayload {
    public static final Type<PhotoCaptureResultPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "photo_capture_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PhotoCaptureResultPacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, PhotoCaptureResultPacket packet) ->
                    encode(packet, buffer), PhotoCaptureResultPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void encode(PhotoCaptureResultPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.uploadId);
        buffer.writeBoolean(packet.success);
    }

    public static PhotoCaptureResultPacket decode(FriendlyByteBuf buffer) {
        return new PhotoCaptureResultPacket(buffer.readUUID(), buffer.readBoolean());
    }

}
