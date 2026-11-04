package EdDYON.guaniao.fabric;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Real Fabric 26.2 compile gate for photo transfer payloads and bounded chunks.
 * This is intentionally independent of unported camera classes.
 */
public final class GuaniaoPhotoTransferPacketProof {
    private static final int CHUNK_LIMIT = 16384;

    public record Chunk(UUID uploadId, int index, byte[] data) implements CustomPacketPayload {
        public static final Type<Chunk> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("guaniao", "photo_chunk_proof"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Chunk> CODEC =
                StreamCodec.of((RegistryFriendlyByteBuf buffer, Chunk packet) ->
                        encode(packet, buffer), Chunk::decode);

        private static void encode(Chunk packet, FriendlyByteBuf buffer) {
            if (packet.data().length <= 0 || packet.data().length > CHUNK_LIMIT) {
                throw new IllegalArgumentException("Invalid chunk length");
            }
            buffer.writeUUID(packet.uploadId());
            buffer.writeVarInt(packet.index());
            buffer.writeByteArray(packet.data());
        }

        private static Chunk decode(FriendlyByteBuf buffer) {
            return new Chunk(buffer.readUUID(), buffer.readVarInt(), buffer.readByteArray(CHUNK_LIMIT));
        }

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private GuaniaoPhotoTransferPacketProof() { }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(Chunk.TYPE, Chunk.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Chunk.TYPE, (payload, context) ->
                context.player().getUUID());
    }
}
