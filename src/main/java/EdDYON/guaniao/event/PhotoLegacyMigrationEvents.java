package EdDYON.guaniao.event;

import EdDYON.guaniao.content.camera.LegacyPhotoMigration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Preserve legacy photograph migration scans on login and on newly opened
 * server containers without relying on Forge's PlayerContainerEvent.Open.
 *
 * Fabric has no equivalent general server-side container-open callback.
 * Observe menu identity once per tick; each newly opened menu is scanned once
 * and the existing LegacyPhotoMigration queue performs the actual conversion.
 * The observer does not rescan slots in unchanged menus.
 */
public final class PhotoLegacyMigrationEvents {
    private static final Map<UUID, AbstractContainerMenu> PREVIOUS_MENUS = new HashMap<>();

    private PhotoLegacyMigrationEvents() { }

    public static void onPlayerLogin(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            LegacyPhotoMigration.queue(player.level(), player.getInventory().getItem(slot));
        }
    }

    public static void onContainerOpen(ServerPlayer player, AbstractContainerMenu menu) {
        for (Slot slot : menu.slots) {
            LegacyPhotoMigration.queue(player.level(), slot.getItem());
        }
    }

    public static void onServerTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            AbstractContainerMenu opened = player.containerMenu;
            AbstractContainerMenu previous = PREVIOUS_MENUS.put(player.getUUID(), opened);
            if (opened != previous && opened != player.inventoryMenu) {
                onContainerOpen(player, opened);
            }
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
        PREVIOUS_MENUS.remove(player.getUUID());
    }

    public static void onServerStopping(MinecraftServer server) {
        PREVIOUS_MENUS.clear();
    }

    public static void registerCallbacks() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                onPlayerLogin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                onPlayerLogout(handler.getPlayer()));
        ServerTickEvents.START_SERVER_TICK.register(PhotoLegacyMigrationEvents::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(PhotoLegacyMigrationEvents::onServerStopping);
    }
}
