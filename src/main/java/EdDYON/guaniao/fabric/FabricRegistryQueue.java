package EdDYON.guaniao.fabric;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Fabric-native substitute for the SOURCE's deferred-registry registration pattern.
 * It is deliberately not a Forge implementation. This queue preserves the mod's
 * original identifiers and the time at which objects are constructed.
 *
 * Registration is only permitted once, during Fabric's initialization phase.
 * Callers continue to retrieve registered objects through Supplier.get().
 */
public final class FabricRegistryQueue<T> {
    private final Registry<T> registry;
    private final String namespace;
    private final List<Runnable> pending = new ArrayList<>();
    private final Set<Identifier> ids = new HashSet<>();
    private boolean registered;

    public FabricRegistryQueue(Registry<T> registry, String namespace) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.namespace = Objects.requireNonNull(namespace, "namespace");
    }

    public <R extends T> Ref<R> register(String path, Supplier<? extends R> factory) {
        if (registered) {
            throw new IllegalStateException("Cannot register after registry queue has been flushed: " + namespace);
        }
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        if (!ids.add(id)) {
            throw new IllegalArgumentException("Duplicate registry id: " + id);
        }
        Objects.requireNonNull(factory, "factory");
        Ref<R> ref = new Ref<>(id);
        pending.add(() -> ref.set(Registry.register(registry, id, factory.get())));
        return ref;
    }

    public void registerAll() {
        if (registered) {
            throw new IllegalStateException("Registry queue already flushed: " + namespace);
        }
        registered = true;
        for (Runnable task : pending) {
            task.run();
        }
        pending.clear();
    }

    /** Supplier preserves original call-site semantics of the old RegistryObject.get(). */
    public static final class Ref<R> implements Supplier<R> {
        private final Identifier id;
        private R value;

        private Ref(Identifier id) {
            this.id = id;
        }

        private void set(R value) {
            if (this.value != null) {
                throw new IllegalStateException("Registered twice: " + id);
            }
            this.value = Objects.requireNonNull(value, "registered value");
        }

        @Override
        public R get() {
            if (value == null) {
                throw new IllegalStateException("Registered object referenced before Fabric initialization: " + id);
            }
            return value;
        }
    }
}
