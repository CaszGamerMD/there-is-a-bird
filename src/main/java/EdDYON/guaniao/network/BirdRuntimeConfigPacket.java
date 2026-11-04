package EdDYON.guaniao.network;

import EdDYON.guaniao.GuaniaoMod;
import EdDYON.guaniao.config.BirdConfigManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server-authoritative three-boolean runtime configuration snapshot.
 * Wire order is unchanged: leaves, April Fools, sky-bird ecology.
 */
public record BirdRuntimeConfigPacket(boolean birdsPassThroughLeaves, boolean aprilFoolsMode,
                                      boolean skyBirdEcologyEnabled) implements CustomPacketPayload {
    public static final Type<BirdRuntimeConfigPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "bird_runtime_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BirdRuntimeConfigPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, BirdRuntimeConfigPacket::birdsPassThroughLeaves,
                    ByteBufCodecs.BOOL, BirdRuntimeConfigPacket::aprilFoolsMode,
                    ByteBufCodecs.BOOL, BirdRuntimeConfigPacket::skyBirdEcologyEnabled,
                    BirdRuntimeConfigPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void applyOnClient() {
        BirdConfigManager.applyRemoteRuntime(
                birdsPassThroughLeaves, aprilFoolsMode, skyBirdEcologyEnabled);
    }
}
