package EdDYON.guaniao.fabric;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.commands.Commands;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;

/** Compile-only 26.2 contract for frame previews, packet codecs and permissions. */
public final class GuaniaoFinalNetworkingProof {
    public record FramePreview(int window, ItemStack preview) implements CustomPacketPayload {
        public static final Type<FramePreview> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("guaniao", "frame_preview_proof"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FramePreview> CODEC =
                StreamCodec.of((RegistryFriendlyByteBuf buffer, FramePreview packet) -> {
                    buffer.writeVarInt(packet.window());
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, packet.preview());
                }, (RegistryFriendlyByteBuf buffer) ->
                        new FramePreview(buffer.readVarInt(),
                                ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)));

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private GuaniaoFinalNetworkingProof() { }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(FramePreview.TYPE, FramePreview.CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("guaniao_probe")
                        .requires(source -> source.permissions().hasPermission(
                                Permissions.COMMANDS_MODERATOR))
                        .executes(context -> 1)));
    }
}
