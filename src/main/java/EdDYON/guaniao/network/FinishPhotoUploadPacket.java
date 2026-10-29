package EdDYON.guaniao.network;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FinishPhotoUploadPacket(UUID uploadId) implements CustomPacketPayload {
    public static final Type<FinishPhotoUploadPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "finish_photo_upload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FinishPhotoUploadPacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, FinishPhotoUploadPacket packet) ->
                    encode(packet, buffer), FinishPhotoUploadPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void encode(FinishPhotoUploadPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.uploadId);
    }

    public static FinishPhotoUploadPacket decode(FriendlyByteBuf buffer) {
        return new FinishPhotoUploadPacket(buffer.readUUID());
    }

    public void handle(ServerPlayer player) {
        PhotoUploadManager.finish(player, uploadId);
    }
}
