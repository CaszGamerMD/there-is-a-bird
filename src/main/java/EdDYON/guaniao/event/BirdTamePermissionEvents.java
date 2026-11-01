package EdDYON.guaniao.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

/** Fabric replacement for a cancellable pre-tame hook. The original feeding cost still applies on denial. */
public final class BirdTamePermissionEvents {
    @FunctionalInterface
    public interface Allow {
        /** Returning false vetoes the tame; no ownership or command state should be changed. */
        boolean canTame(TamableAnimal bird, Player player);
    }

    public static final Event<Allow> BEFORE_TAME = EventFactory.createArrayBacked(Allow.class,
            listeners -> (bird, player) -> {
                for (Allow listener : listeners) if (!listener.canTame(bird, player)) return false;
                return true;
            });

    private BirdTamePermissionEvents() { }
}
