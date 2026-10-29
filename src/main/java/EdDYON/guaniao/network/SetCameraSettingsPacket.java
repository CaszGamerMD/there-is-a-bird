package EdDYON.guaniao.network;

import EdDYON.guaniao.content.camera.CameraAperture;
import EdDYON.guaniao.content.camera.CameraFilter;
import EdDYON.guaniao.content.camera.CameraFocusMode;
import EdDYON.guaniao.content.camera.CameraLens;
import EdDYON.guaniao.content.camera.CameraSettingsData;
import EdDYON.guaniao.content.camera.CameraShootingMode;
import EdDYON.guaniao.content.camera.CameraState;
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

public record SetCameraSettingsPacket(InteractionHand hand, CameraState state) implements CustomPacketPayload {
    public static final Type<SetCameraSettingsPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "set_camera_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetCameraSettingsPacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, SetCameraSettingsPacket packet) ->
                    encode(packet, buffer), SetCameraSettingsPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void encode(SetCameraSettingsPacket packet, FriendlyByteBuf buffer) {
        CameraState state = packet.state;
        buffer.writeEnum(packet.hand);
        buffer.writeVarInt(state.filter().id());
        buffer.writeVarInt(state.lens().id());
        buffer.writeVarInt(state.shootingMode().id());
        buffer.writeDouble(state.focalLength());
        buffer.writeVarInt(state.aperture().id());
        buffer.writeVarInt(state.focusMode().id());
        buffer.writeDouble(state.focusDistance());
    }

    public static SetCameraSettingsPacket decode(FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readEnum(InteractionHand.class);
        CameraState state = new CameraState(
                CameraFilter.byId(buffer.readVarInt()),
                CameraLens.byId(buffer.readVarInt()),
                CameraShootingMode.byId(buffer.readVarInt()),
                buffer.readDouble(),
                CameraAperture.byId(buffer.readVarInt()),
                CameraFocusMode.byId(buffer.readVarInt()),
                buffer.readDouble()
        );
        return new SetCameraSettingsPacket(hand, state);
    }

    public void handle(ServerPlayer player) {
        if (!Double.isFinite(state.focalLength()) || !Double.isFinite(state.focusDistance())) return;
        ItemStack camera = player.getItemInHand(hand);
        if (camera.is(GuaniaoItems.NIKON_D750.get())) {
            CameraSettingsData.setState(camera, state);
        }
    }
}
