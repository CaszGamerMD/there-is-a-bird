package EdDYON.guaniao.content.bird.hummingbird;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;

/** Automation permission hook, not a player bone-meal or natural random-growth event. */
public final class HummingbirdPollinationEvent {
    @FunctionalInterface
    public interface BeforeGrow {
        /** Returning false vetoes this scheduled pollination without modifying the crop. */
        boolean allow(HummingbirdPollinationEvent event);
    }

    @FunctionalInterface
    public interface MobGriefing {
        /** Return false to deny this bird automated crop growth. The vanilla gamerule is checked separately. */
        boolean allow(ServerLevel level, Mob bird);
    }

    public static final Event<BeforeGrow> BEFORE_GROW = EventFactory.createArrayBacked(BeforeGrow.class,
            listeners -> event -> {
                for (BeforeGrow listener : listeners) {
                    if (!listener.allow(event)) return false;
                }
                return true;
            });

    public static final Event<MobGriefing> MOB_GRIEFING = EventFactory.createArrayBacked(MobGriefing.class,
            listeners -> (level, bird) -> {
                for (MobGriefing listener : listeners) {
                    if (!listener.allow(level, bird)) return false;
                }
                return true;
            });

    private final ServerLevel level;
    private final BlockPos target;
    private final BlockState state;
    private final long round;
    private final List<HummingbirdPollinationService.Source> sources;

    public HummingbirdPollinationEvent(ServerLevel level, BlockPos target, BlockState state, long round,
                                      List<HummingbirdPollinationService.Source> sources) {
        this.level = level; this.target = target.immutable(); this.state = state; this.round = round;
        this.sources = List.copyOf(sources);
    }
    public ServerLevel getLevel() { return level; }
    public BlockPos getTarget() { return target; }
    public BlockState getState() { return state; }
    public long getRound() { return round; }
    public List<HummingbirdPollinationService.Source> getSources() { return sources; }
    public Set<UUID> getOwners() {
        return sources.stream().map(HummingbirdPollinationService.Source::owner)
                .filter(java.util.Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    }
}
