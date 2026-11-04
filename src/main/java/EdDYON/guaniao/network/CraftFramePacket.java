package EdDYON.guaniao.network;

import EdDYON.guaniao.content.camera.frame.FrameDesign;
import EdDYON.guaniao.content.camera.frame.FrameWorkbenchMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public record CraftFramePacket(int containerId, int revision, @Nullable FrameDesign design) implements CustomPacketPayload {
    public static final Type<CraftFramePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "craft_frame"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftFramePacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, CraftFramePacket packet) ->
                    encode(packet, buffer), CraftFramePacket::decode);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void encode(CraftFramePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.containerId);
        buffer.writeVarInt(packet.revision);
        if (packet.design == null) throw new IllegalArgumentException("A frame design is required");
        packet.design.writeTo(buffer);
    }

    public static CraftFramePacket decode(FriendlyByteBuf buffer) {
        return new CraftFramePacket(buffer.readVarInt(), buffer.readVarInt(), FrameDesign.readFrom(buffer));
    }

    public void handle(ServerPlayer player) {
        if (design != null && player.containerMenu instanceof FrameWorkbenchMenu menu
                && menu.containerId == containerId) {
            menu.craft(player, revision, design);
        }
    }
}
