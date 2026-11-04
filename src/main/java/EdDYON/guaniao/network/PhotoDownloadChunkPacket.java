package EdDYON.guaniao.network;

import EdDYON.guaniao.content.camera.PhotoTransferLimits;
import net.minecraft.network.FriendlyByteBuf;
import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PhotoDownloadChunkPacket(String photoId, int chunkIndex, byte[] data) implements CustomPacketPayload {
    public static final Type<PhotoDownloadChunkPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "photo_download_chunk"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PhotoDownloadChunkPacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, PhotoDownloadChunkPacket packet) ->
                    encode(packet, buffer), PhotoDownloadChunkPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void encode(PhotoDownloadChunkPacket packet, FriendlyByteBuf buffer) {
        if (packet.data.length <= 0 || packet.data.length > PhotoTransferLimits.MAX_CHUNK_BYTES) {
            throw new IllegalArgumentException("Invalid photograph chunk size");
        }
        buffer.writeUtf(packet.photoId, PhotoTransferLimits.MAX_PHOTO_ID_LENGTH);
        buffer.writeVarInt(packet.chunkIndex);
        buffer.writeByteArray(packet.data);
    }

    public static PhotoDownloadChunkPacket decode(FriendlyByteBuf buffer) {
        return new PhotoDownloadChunkPacket(
                buffer.readUtf(PhotoTransferLimits.MAX_PHOTO_ID_LENGTH),
                buffer.readVarInt(),
                buffer.readByteArray(PhotoTransferLimits.MAX_CHUNK_BYTES)
        );
    }

}
