package EdDYON.guaniao.network;

import EdDYON.guaniao.content.camera.PhotoTransferLimits;
import net.minecraft.network.FriendlyByteBuf;
import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PhotoDownloadStartPacket(
        String photoId,
        boolean found,
        int totalBytes,
        int width,
        int height,
        String contentHash
) implements CustomPacketPayload {
    public static PhotoDownloadStartPacket missing(String photoId) {
        return new PhotoDownloadStartPacket(photoId, false, 0, 0, 0, "");
    }

    public static final Type<PhotoDownloadStartPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "photo_download_start"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PhotoDownloadStartPacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, PhotoDownloadStartPacket packet) ->
                    encode(packet, buffer), PhotoDownloadStartPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void encode(PhotoDownloadStartPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.photoId, PhotoTransferLimits.MAX_PHOTO_ID_LENGTH);
        buffer.writeBoolean(packet.found);
        buffer.writeVarInt(packet.totalBytes);
        buffer.writeVarInt(packet.width);
        buffer.writeVarInt(packet.height);
        buffer.writeUtf(packet.contentHash, PhotoTransferLimits.SHA256_HEX_LENGTH);
    }

    public static PhotoDownloadStartPacket decode(FriendlyByteBuf buffer) {
        return new PhotoDownloadStartPacket(
                buffer.readUtf(PhotoTransferLimits.MAX_PHOTO_ID_LENGTH),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readUtf(PhotoTransferLimits.SHA256_HEX_LENGTH)
        );
    }

}
