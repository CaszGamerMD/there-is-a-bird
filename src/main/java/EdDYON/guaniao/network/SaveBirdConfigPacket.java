package EdDYON.guaniao.network;

import EdDYON.guaniao.config.BirdConfigData;
import EdDYON.guaniao.config.BirdConfigManager;
import EdDYON.guaniao.config.BirdGlobalConfig;
import EdDYON.guaniao.config.BirdSpeciesConfig;
import EdDYON.guaniao.event.BirdDroppingEvents;
import EdDYON.guaniao.event.BirdPopulationTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public final class SaveBirdConfigPacket implements CustomPacketPayload {
    public static final Type<SaveBirdConfigPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "save_bird_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SaveBirdConfigPacket> CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, SaveBirdConfigPacket packet) ->
                    encode(packet, buffer), SaveBirdConfigPacket::decode);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    private final BirdConfigData data;

    public SaveBirdConfigPacket(BirdConfigData data) {
        this.data = data.copy();
    }

    public static void encode(SaveBirdConfigPacket packet, FriendlyByteBuf buffer) {
        BirdConfigPacketCodec.encode(buffer, packet.data);
    }

    public static SaveBirdConfigPacket decode(FriendlyByteBuf buffer) {
        return new SaveBirdConfigPacket(BirdConfigPacketCodec.decode(buffer));
    }

    public void handle(ServerPlayer player) {
            if (player == null || (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)
                    && (player.server == null || !player.server.isSingleplayerOwner(player.getGameProfile())))) {
                return;
            }
            // Full-world scans (population re-tally, per-bird dropping cooldowns) are expensive;
            // only run them when the fields that drive them actually changed.
            BirdConfigData before = BirdConfigManager.snapshot();
            if (BirdConfigManager.replaceAndSave(data, player.server)) {
                BirdConfigData after = BirdConfigManager.snapshot();
                if (before.global.populationRegionChunks != after.global.populationRegionChunks) {
                    BirdPopulationTracker.rebuild(player.server);
                }
                if (droppingsChanged(before, after)) {
                    BirdDroppingEvents.refreshLoadedBirdCooldowns(player.server);
                }
                if (ServerPlayNetworking.canSend(player, OpenBirdConfigPacket.TYPE)) {
                    ServerPlayNetworking.send(player, new OpenBirdConfigPacket(BirdConfigManager.snapshot()));
                }
                for (ServerPlayer online : player.server.getPlayerList().getPlayers()) {
                    if (ServerPlayNetworking.canSend(online, BirdRuntimeConfigPacket.TYPE)) {
                        ServerPlayNetworking.send(online, new BirdRuntimeConfigPacket(
                                BirdConfigManager.birdsPassThroughLeaves(),
                                BirdConfigManager.aprilFoolsMode(),
                                BirdConfigManager.skyBirdEcologyEnabled()));
                    }
                }
            }
    }

    private static boolean droppingsChanged(BirdConfigData before, BirdConfigData after) {
        BirdGlobalConfig oldGlobal = before.global;
        BirdGlobalConfig newGlobal = after.global;
        if (Double.compare(oldGlobal.droppingFrequencyMultiplier, newGlobal.droppingFrequencyMultiplier) != 0
                || oldGlobal.naturalDroppingsEnabled != newGlobal.naturalDroppingsEnabled
                || oldGlobal.droppingPressurePlatePulseEnabled != newGlobal.droppingPressurePlatePulseEnabled
                || oldGlobal.maxGroundDroppingsNearby != newGlobal.maxGroundDroppingsNearby
                || oldGlobal.droppingPressurePlatePulseTicks != newGlobal.droppingPressurePlatePulseTicks) {
            return true;
        }
        Set<String> speciesKeys = new HashSet<>(before.birds.keySet());
        speciesKeys.addAll(after.birds.keySet());
        for (String key : speciesKeys) {
            BirdSpeciesConfig oldSpecies = before.birds.get(key);
            BirdSpeciesConfig newSpecies = after.birds.get(key);
            double oldMultiplier = oldSpecies == null ? 1.0D : oldSpecies.droppingFrequencyMultiplier;
            double newMultiplier = newSpecies == null ? 1.0D : newSpecies.droppingFrequencyMultiplier;
            if (Double.compare(oldMultiplier, newMultiplier) != 0) {
                return true;
            }
        }
        return false;
    }
}
