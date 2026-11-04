package EdDYON.guaniao.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** An owner-only display snapshot; the preview is never an inventory slot. */
public record FrameWorkSnapshotPacket(int containerId, BlockPos pos, ItemStack preview,
                                      long seed, int remaining, int revision) implements CustomPacketPayload {
    public static final Type<FrameWorkSnapshotPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "frame_work_snapshot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FrameWorkSnapshotPacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, FrameWorkSnapshotPacket packet) ->
                    encode(packet, buffer), FrameWorkSnapshotPacket::decode);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void encode(FrameWorkSnapshotPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.containerId);
        buffer.writeBlockPos(packet.pos);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, packet.preview);
        buffer.writeLong(packet.seed);
        buffer.writeVarInt(packet.remaining);
        buffer.writeVarInt(packet.revision);
    }

    public static FrameWorkSnapshotPacket decode(RegistryFriendlyByteBuf buffer) {
        return new FrameWorkSnapshotPacket(buffer.readVarInt(), buffer.readBlockPos(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                buffer.readLong(), buffer.readVarInt(), buffer.readVarInt());
    }

}
