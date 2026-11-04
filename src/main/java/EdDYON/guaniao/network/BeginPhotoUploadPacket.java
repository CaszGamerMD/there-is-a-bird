package EdDYON.guaniao.network;

import EdDYON.guaniao.content.camera.PhotoTransferLimits;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BeginPhotoUploadPacket(
        UUID uploadId,
        InteractionHand hand,
        int totalBytes,
        int width,
        int height,
        String contentHash
) implements CustomPacketPayload {
    public static final Type<BeginPhotoUploadPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "begin_photo_upload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BeginPhotoUploadPacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, BeginPhotoUploadPacket packet) ->
                    encode(packet, buffer), BeginPhotoUploadPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void encode(BeginPhotoUploadPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.uploadId);
        buffer.writeEnum(packet.hand);
        buffer.writeVarInt(packet.totalBytes);
        buffer.writeVarInt(packet.width);
        buffer.writeVarInt(packet.height);
        buffer.writeUtf(packet.contentHash, PhotoTransferLimits.SHA256_HEX_LENGTH);
    }

    public static BeginPhotoUploadPacket decode(FriendlyByteBuf buffer) {
        return new BeginPhotoUploadPacket(
                buffer.readUUID(),
                buffer.readEnum(InteractionHand.class),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readUtf(PhotoTransferLimits.SHA256_HEX_LENGTH)
        );
    }

    public void handle(ServerPlayer player) {
        PhotoUploadManager.begin(player, uploadId, hand, totalBytes, width, height, contentHash);
    }
}
