package EdDYON.guaniao.fabric;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/** Compile-only proof for a short-circuiting, cancellable Fabric hook. */
public final class GuaniaoCaptureEventProof {
    private GuaniaoCaptureEventProof() { }

    @FunctionalInterface
    public interface Listener {
        boolean cancel(int candidate);
    }

    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(
            Listener.class, listeners -> candidate -> {
                for (Listener listener : listeners) {
                    if (listener.cancel(candidate)) return true;
                }
                return false;
            });
}
