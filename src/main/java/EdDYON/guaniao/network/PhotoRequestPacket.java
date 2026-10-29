package EdDYON.guaniao.network;

import EdDYON.guaniao.content.camera.PhotoTransferLimits;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PhotoRequestPacket(String photoId, String expectedHash) implements CustomPacketPayload {
    public static final Type<PhotoRequestPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "photo_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PhotoRequestPacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, PhotoRequestPacket packet) ->
                    encode(packet, buffer), PhotoRequestPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void encode(PhotoRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.photoId, PhotoTransferLimits.MAX_PHOTO_ID_LENGTH);
        buffer.writeUtf(packet.expectedHash, PhotoTransferLimits.SHA256_HEX_LENGTH);
    }

    public static PhotoRequestPacket decode(FriendlyByteBuf buffer) {
        return new PhotoRequestPacket(
                buffer.readUtf(PhotoTransferLimits.MAX_PHOTO_ID_LENGTH),
                buffer.readUtf(PhotoTransferLimits.SHA256_HEX_LENGTH)
        );
    }

    public void handle(ServerPlayer player) {
        PhotoUploadManager.requestDownload(player, photoId, expectedHash);
    }
}
