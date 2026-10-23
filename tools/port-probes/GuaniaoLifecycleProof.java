package EdDYON.guaniao.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import java.nio.file.Path;

/**
 * Compile-only 26.2 event signature probe. Never call this method:
 * these are intentionally no-op callbacks and not part of the final mod.
 */
public final class GuaniaoLifecycleProof {
    private GuaniaoLifecycleProof() { }

    public static void probe() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> { });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> { });
        ServerLevelEvents.UNLOAD.register((server, level) -> { });
        ServerTickEvents.END_SERVER_TICK.register(server -> { });
        ServerTickEvents.START_SERVER_TICK.register(server -> { });
        ServerTickEvents.END_WORLD_TICK.register(level -> { });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> { });
        ServerLifecycleEvents.SERVER_STARTING.register(server -> { });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> { });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> { });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> { });
        EntityTrackingEvents.START_TRACKING.register((target, player) -> { });
        EntityTrackingEvents.STOP_TRACKING.register((target, player) -> { });
    }

    public static void probeLegacyMenu(ServerPlayer player) {
        Path config = FabricLoader.getInstance().getConfigDir();
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != player.inventoryMenu) {
            for (Slot slot : menu.slots) {
                slot.getItem();
            }
        }
        player.getInventory().getContainerSize();
    }
}
