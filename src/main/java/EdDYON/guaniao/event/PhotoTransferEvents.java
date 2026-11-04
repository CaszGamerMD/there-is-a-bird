package EdDYON.guaniao.event;

import EdDYON.guaniao.network.PhotoUploadManager;
import EdDYON.guaniao.content.camera.LegacyPhotoMigration;
import EdDYON.guaniao.content.camera.PhotoIndexSavedData;
import EdDYON.guaniao.content.camera.PhotoIoService;
import EdDYON.guaniao.content.camera.PhotoMaintenance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;

public final class PhotoTransferEvents {
    private static long nextMaintenanceTick;
    private PhotoTransferEvents() {
    }

    public static void onServerTick(MinecraftServer server) {
        PhotoUploadManager.tick(server);
        LegacyPhotoMigration.tick(server);
        long now = server.overworld().getGameTime();
        if (now >= nextMaintenanceTick && !PhotoMaintenance.isRunning()) {
            nextMaintenanceTick = now + 36_000L;
            PhotoMaintenance.scheduleAutomatic(server, result -> { });
        }
    }

    public static void onServerStarted(MinecraftServer server) {
        PhotoIndexSavedData.get(server);
        nextMaintenanceTick = server.overworld().getGameTime() + 200L;
    }

    public static void onPlayerLogout(ServerPlayer player) {
        PhotoUploadManager.disconnect(player.getUUID());
    }

    public static void onStartTracking(Entity target, ServerPlayer player) {
        PhotoUploadManager.startTracking(player, target);
    }

    public static void onStopTracking(Entity target, ServerPlayer player) {
        PhotoUploadManager.stopTracking(player, target);
    }

    public static void onServerStopping(MinecraftServer server) {
        PhotoUploadManager.clear();
        LegacyPhotoMigration.clear();
        PhotoMaintenance.reset();
        PhotoIoService.shutdown();
        nextMaintenanceTick = 0L;
    }

    /** Fabric lifecycle callbacks in place of Forge event subscribers. */
    public static void registerCallbacks() {
        ServerTickEvents.END_SERVER_TICK.register(PhotoTransferEvents::onServerTick);
        ServerLifecycleEvents.SERVER_STARTED.register(PhotoTransferEvents::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(PhotoTransferEvents::onServerStopping);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                onPlayerLogout(handler.getPlayer()));
        EntityTrackingEvents.START_TRACKING.register(PhotoTransferEvents::onStartTracking);
        EntityTrackingEvents.STOP_TRACKING.register(PhotoTransferEvents::onStopTracking);
    }
}
