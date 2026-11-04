package EdDYON.guaniao.content.bird.nightheron;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.animal.fish.AbstractFish;

/**
 * Cancellable server-side permission hook before converting a living fish to
 * held food. This remains separate from entity death and loot events.
 */
public final class NightHeronFishCaptureEvent {
    @FunctionalInterface
    public interface Listener {
        /** Return true to cancel capture without removing or harming the fish. */
        boolean cancelCapture(NightHeronFishCaptureEvent event);
    }

    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(
            Listener.class, listeners -> attempt -> {
                for (Listener listener : listeners) {
                    if (listener.cancelCapture(attempt)) return true;
                }
                return false;
            });

    private final NightHeronEntity bird;
    private final AbstractFish fish;
    private final HeldFishPurpose purpose;

    public NightHeronFishCaptureEvent(NightHeronEntity bird, AbstractFish fish, HeldFishPurpose purpose) {
        this.bird = bird;
        this.fish = fish;
        this.purpose = purpose;
    }

    public static boolean isCancelled(NightHeronEntity bird, AbstractFish fish, HeldFishPurpose purpose) {
        return EVENT.invoker().cancelCapture(new NightHeronFishCaptureEvent(bird, fish, purpose));
    }

    public NightHeronEntity getBird() { return this.bird; }
    public AbstractFish getFish() { return this.fish; }
    public HeldFishPurpose getPurpose() { return this.purpose; }
}
