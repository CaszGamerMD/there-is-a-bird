package EdDYON.guaniao.network;

import EdDYON.guaniao.content.camera.CameraFilter;
import EdDYON.guaniao.content.camera.CameraSettingsData;
import EdDYON.guaniao.registry.GuaniaoItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetCameraFilterPacket(InteractionHand hand, CameraFilter filter) implements CustomPacketPayload {
    public static final Type<SetCameraFilterPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "set_camera_filter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetCameraFilterPacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, SetCameraFilterPacket packet) ->
                    encode(packet, buffer), SetCameraFilterPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void encode(SetCameraFilterPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.hand);
        buffer.writeVarInt(packet.filter.id());
    }

    public static SetCameraFilterPacket decode(FriendlyByteBuf buffer) {
        return new SetCameraFilterPacket(buffer.readEnum(InteractionHand.class), CameraFilter.byId(buffer.readVarInt()));
    }

    public void handle(ServerPlayer player) {
        ItemStack camera = player.getItemInHand(hand);
        if (camera.is(GuaniaoItems.NIKON_D750.get())) {
            CameraSettingsData.setFilter(camera, filter);
        }
    }
}
