package EdDYON.guaniao.event;

import EdDYON.guaniao.content.bird.command.BirdHeadPlacement;
import EdDYON.guaniao.content.bird.hummingbird.HummingbirdEntity;
import EdDYON.guaniao.content.bird.kestrel.KestrelEntity;
import EdDYON.guaniao.content.bird.umbrellacockatoo.UmbrellaCockatooEntity;
import net.minecraft.core.Direction;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.Vec3;

public final class BirdHeadPerchEvents {
    private BirdHeadPerchEvents() { }

    public static void onServerStopping(MinecraftServer server) {
        // Passenger birds are skipped by world saves; detach before players are saved.
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            releaseHeadBirds(player);
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
        releaseHeadBirds(player);
    }

    private static void releaseHeadBirds(ServerPlayer player) {
        for (Entity passenger : java.util.List.copyOf(player.getPassengers())) {
            if (!(passenger instanceof HummingbirdEntity || passenger instanceof KestrelEntity
                    || passenger instanceof UmbrellaCockatooEntity)
                    || !(passenger instanceof TamableAnimal bird) || !bird.isTame() || !bird.isOwnedBy(player)) continue;
            bird.stopRiding();
            if (bird.isPassenger()) continue;
            bird.setDeltaMovement(Vec3.ZERO);
            bird.setNoGravity(false);
            bird.resetFallDistance();
        }
    }

    /**
     * Fabric invokes this prior to vanilla block handling, and a returned SUCCESS
     * suppresses vanilla use while allowing the normal client -> server packet.
     * Earlier Fabric callbacks that consume/deny interactions stop invocation.
     */
    public static InteractionResult onRightClickBlock(Player owner, Level level, InteractionHand hand, BlockHitResult hit) {
        if (owner.isSpectator() || hit.getDirection() != Direction.UP
                || !BirdHeadPlacement.isPlacementGesture(owner, hand)) {
            return InteractionResult.PASS;
        }
        boolean placed = level.isClientSide()
                || BirdHeadPlacement.tryPlace(owner, hand, hit);
        return placed ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public static void registerCallbacks() {
        ServerLifecycleEvents.SERVER_STOPPING.register(BirdHeadPerchEvents::onServerStopping);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                onPlayerLogout(handler.getPlayer()));
        UseBlockCallback.EVENT.register(BirdHeadPerchEvents::onRightClickBlock);
    }
}
