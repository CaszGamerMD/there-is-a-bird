package EdDYON.guaniao.network;

import EdDYON.guaniao.content.camera.PhotoTransferLimits;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PhotoUploadChunkPacket(UUID uploadId, int chunkIndex, byte[] data) implements CustomPacketPayload {
    public static final Type<PhotoUploadChunkPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "photo_upload_chunk"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PhotoUploadChunkPacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, PhotoUploadChunkPacket packet) ->
                    encode(packet, buffer), PhotoUploadChunkPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void encode(PhotoUploadChunkPacket packet, FriendlyByteBuf buffer) {
        if (packet.data.length <= 0 || packet.data.length > PhotoTransferLimits.MAX_CHUNK_BYTES) {
            throw new IllegalArgumentException("Invalid photograph chunk size");
        }
        buffer.writeUUID(packet.uploadId);
        buffer.writeVarInt(packet.chunkIndex);
        buffer.writeByteArray(packet.data);
    }

    public static PhotoUploadChunkPacket decode(FriendlyByteBuf buffer) {
        return new PhotoUploadChunkPacket(
                buffer.readUUID(),
                buffer.readVarInt(),
                buffer.readByteArray(PhotoTransferLimits.MAX_CHUNK_BYTES)
        );
    }

    public void handle(ServerPlayer player) {
        PhotoUploadManager.acceptChunk(player, uploadId, chunkIndex, data);
    }
}
