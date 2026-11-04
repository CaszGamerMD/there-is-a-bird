package EdDYON.guaniao.network;

import EdDYON.guaniao.GuaniaoMod;
import EdDYON.guaniao.content.fan.FeatherFanItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Zero-argument Fabric play C2S payload for the fully charged fan left-click.
 * Never trust client-supplied charging state: the server rechecks the use item.
 */
public record FeatherFanPiercePacket() implements CustomPacketPayload {
    public static final Type<FeatherFanPiercePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "feather_fan_pierce"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FeatherFanPiercePacket> CODEC =
            StreamCodec.unit(new FeatherFanPiercePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player) {
        if (player.getUseItem().getItem() instanceof FeatherFanItem fan) {
            fan.tryLaunchPiercing(player);
        }
    }
}
