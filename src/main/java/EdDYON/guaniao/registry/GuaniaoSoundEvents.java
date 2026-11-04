package EdDYON.guaniao.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;

public final class GuaniaoSoundEvents {
    private static final String MOD_ID = "guaniao";
    private static final Map<String, Supplier<SoundEvent>> PENDING = new LinkedHashMap<>();
    private static final Map<String, SoundEvent> REGISTERED = new HashMap<>();

    public static final Supplier<SoundEvent> NIGHT_HERON_AMBIENT = register("entity.night_heron.ambient");
    public static final Supplier<SoundEvent> NIGHT_HERON_HURT = register("entity.night_heron.hurt");
    public static final Supplier<SoundEvent> NIGHT_HERON_DEATH = register("entity.night_heron.death");
    public static final Supplier<SoundEvent> NIGHT_HERON_ATTACK = register("entity.night_heron.attack");
    public static final Supplier<SoundEvent> CROW_AMBIENT = register("entity.crow.ambient");
    public static final Supplier<SoundEvent> CROW_HURT = register("entity.crow.hurt");
    public static final Supplier<SoundEvent> SPARROW_AMBIENT = register("entity.sparrow.ambient");
    public static final Supplier<SoundEvent> SPARROW_HURT = register("entity.sparrow.hurt");
    public static final Supplier<SoundEvent> SPARROW_DEATH = register("entity.sparrow.death");
    public static final Supplier<SoundEvent> LONG_TAILED_TIT_AMBIENT = register("entity.long_tailed_tit.ambient");
    public static final Supplier<SoundEvent> LONG_TAILED_TIT_HURT = register("entity.long_tailed_tit.hurt");
    public static final Supplier<SoundEvent> LONG_TAILED_TIT_DEATH = register("entity.long_tailed_tit.death");
    public static final Supplier<SoundEvent> BUDGERIGAR_AMBIENT = register("entity.budgerigar.ambient");
    public static final Supplier<SoundEvent> BUDGERIGAR_HURT = register("entity.budgerigar.hurt");
    public static final Supplier<SoundEvent> BUDGERIGAR_DEATH = register("entity.budgerigar.death");
    public static final Supplier<SoundEvent> BUDGERIGAR_INTERACT = register("entity.budgerigar.interact");
    public static final Supplier<SoundEvent> COCKATIEL_AMBIENT = register("entity.cockatiel.ambient");
    public static final Supplier<SoundEvent> COCKATIEL_TAMED_AMBIENT = register("entity.cockatiel.tamed_ambient");
    public static final Supplier<SoundEvent> COCKATIEL_HURT = register("entity.cockatiel.hurt");
    public static final Supplier<SoundEvent> COCKATIEL_DEATH = register("entity.cockatiel.death");
    public static final Supplier<SoundEvent> MACAW_AMBIENT = register("entity.macaw.ambient");
    public static final Supplier<SoundEvent> MACAW_HURT = register("entity.macaw.hurt");
    public static final Supplier<SoundEvent> MACAW_DEATH = register("entity.macaw.death");
    public static final Supplier<SoundEvent> COCKATOO_AMBIENT = register("entity.cockatoo.ambient");
    public static final Supplier<SoundEvent> COCKATOO_INTERACT = register("entity.cockatoo.interact");
    public static final Supplier<SoundEvent> COCKATOO_HURT = register("entity.cockatoo.hurt");
    public static final Supplier<SoundEvent> COCKATOO_DEATH = register("entity.cockatoo.death");
    public static final Supplier<SoundEvent> UMBRELLA_COCKATOO_TAMED_SPECIAL = register("entity.umbrella_cockatoo.tamed_special");
    public static final Supplier<SoundEvent> UMBRELLA_COCKATOO_CALL_01 = register("entity.umbrella_cockatoo.call_01");
    public static final Supplier<SoundEvent> UMBRELLA_COCKATOO_CALL_02 = register("entity.umbrella_cockatoo.call_02");
    public static final Supplier<SoundEvent> UMBRELLA_COCKATOO_CALL_03 = register("entity.umbrella_cockatoo.call_03");
    public static final Supplier<SoundEvent> UMBRELLA_COCKATOO_CALL_04 = register("entity.umbrella_cockatoo.call_04");
    public static final Supplier<SoundEvent> UMBRELLA_COCKATOO_CALL_05 = register("entity.umbrella_cockatoo.call_05");
    public static final Supplier<SoundEvent> UMBRELLA_COCKATOO_CALL_07 = register("entity.umbrella_cockatoo.call_07");
    public static final Supplier<SoundEvent> UMBRELLA_COCKATOO_CALL_08 = register("entity.umbrella_cockatoo.call_08");
    public static final Supplier<SoundEvent> UMBRELLA_COCKATOO_PHRASE_01 = register("entity.umbrella_cockatoo.phrase_01");
    public static final Supplier<SoundEvent> UMBRELLA_COCKATOO_PHRASE_02 = register("entity.umbrella_cockatoo.phrase_02");
    public static final Supplier<SoundEvent> UMBRELLA_COCKATOO_PHRASE_03 = register("entity.umbrella_cockatoo.phrase_03");
    public static final Supplier<SoundEvent> UMBRELLA_COCKATOO_PHRASE_04 = register("entity.umbrella_cockatoo.phrase_04");
    public static final Supplier<SoundEvent> SPOTTED_DOVE_AMBIENT = register("entity.spotted_dove.ambient");
    public static final Supplier<SoundEvent> SPOTTED_DOVE_HURT = register("entity.spotted_dove.hurt");
    public static final Supplier<SoundEvent> SPOTTED_DOVE_DEATH = register("entity.spotted_dove.death");
    public static final Supplier<SoundEvent> SPOTTED_DOVE_MATE = register("entity.spotted_dove.mate");
    public static final Supplier<SoundEvent> PIGEON_AMBIENT = register("entity.pigeon.ambient");
    public static final Supplier<SoundEvent> KIWI_AMBIENT = register("entity.kiwi.ambient");
    public static final Supplier<SoundEvent> WOODCOCK_AMBIENT = register("entity.woodcock.ambient");
    public static final Supplier<SoundEvent> SEAGULL_AMBIENT = register("entity.seagull.ambient");
    public static final Supplier<SoundEvent> KESTREL_AMBIENT = register("entity.kestrel.ambient");
    public static final Supplier<SoundEvent> KESTREL_HURT = register("entity.kestrel.hurt");
    public static final Supplier<SoundEvent> KINGFISHER_AMBIENT = register("entity.kingfisher.ambient");
    // Match the audio definition: otherwise quiet calls are only sent to players within 16 blocks.
    public static final Supplier<SoundEvent> HUMMINGBIRD_AMBIENT = registerEvent("entity.hummingbird.ambient",
            () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "entity.hummingbird.ambient"), 32F));
    public static final Supplier<SoundEvent> HUMMINGBIRD_HURT = registerEvent("entity.hummingbird.hurt",
            () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "entity.hummingbird.hurt"), 32F));
    public static final Supplier<SoundEvent> HUMMINGBIRD_DEATH = registerEvent("entity.hummingbird.death",
            () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "entity.hummingbird.death"), 32F));
    public static final Supplier<SoundEvent> HUMMINGBIRD_INTERACT = registerEvent("entity.hummingbird.interact",
            () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "entity.hummingbird.interact"), 32F));
    public static final Supplier<SoundEvent> HUMMINGBIRD_WING = register("entity.hummingbird.wing");
    public static final Supplier<SoundEvent> CASSOWARY_AMBIENT = register("entity.cassowary.ambient");
    public static final Supplier<SoundEvent> CASSOWARY_WARNING = register("entity.cassowary.warning");
    public static final Supplier<SoundEvent> CASSOWARY_HURT = register("entity.cassowary.hurt");
    public static final Supplier<SoundEvent> MYNA_CALL_03 = register("entity.myna.call_03");
    public static final Supplier<SoundEvent> MYNA_CALL_04 = register("entity.myna.call_04");
    public static final Supplier<SoundEvent> MYNA_CALL_05 = register("entity.myna.call_05");
    public static final Supplier<SoundEvent> MYNA_CALL_07 = register("entity.myna.call_07");
    public static final Supplier<SoundEvent> MYNA_CALL_08 = register("entity.myna.call_08");
    public static final Supplier<SoundEvent> MYNA_CALL_10 = register("entity.myna.call_10");
    public static final Supplier<SoundEvent> MYNA_CALL_11 = register("entity.myna.call_11");
    public static final Supplier<SoundEvent> MYNA_CALL_12 = register("entity.myna.call_12");
    public static final Supplier<SoundEvent> MYNA_CALL_13 = register("entity.myna.call_13");
    public static final Supplier<SoundEvent> MYNA_TAMED_SPEECH = register("entity.myna.tamed_speech");
    public static final Supplier<SoundEvent> FEATHER_FAN_BURIAL_VORTEX =
            register("item.feather_fan.burial_vortex");
    public static final Supplier<SoundEvent> FEATHER_FAN_BURIAL_SLASH =
            register("item.feather_fan.burial_slash");
    public static final Supplier<SoundEvent> FEATHER_FAN_RIVEN_PIN =
            register("item.feather_fan.riven_pin");
    public static final Supplier<SoundEvent> FEATHER_FAN_RIVEN_SPLIT =
            register("item.feather_fan.riven_split");
    public static final Supplier<SoundEvent> FEATHER_FAN_RIVEN_LOCK =
            register("item.feather_fan.riven_lock");
    public static final Supplier<SoundEvent> FEATHER_FAN_RIVEN_BURST =
            register("item.feather_fan.riven_burst");
    public static final Supplier<SoundEvent> FEATHER_FAN_HUNT_LOCK =
            register("item.feather_fan.hunt_lock");
    public static final Supplier<SoundEvent> FEATHER_FAN_HUNT_START =
            register("item.feather_fan.hunt_start");
    public static final Supplier<SoundEvent> FEATHER_FAN_HUNT_TURN =
            register("item.feather_fan.hunt_turn");
    public static final Supplier<SoundEvent> FEATHER_FAN_HUNT_HIT =
            register("item.feather_fan.hunt_hit");
    public static final Supplier<SoundEvent> MUSIC_DISC_UWU_FUNK =
            register("music_disc.uwu_funk");

    private GuaniaoSoundEvents() {
    }

    /** Called during Fabric's ModInitializer registration phase. */
    public static void registerAll() {
        for (Map.Entry<String, Supplier<SoundEvent>> entry : PENDING.entrySet()) {
            String id = entry.getKey();
            SoundEvent event = Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    Identifier.fromNamespaceAndPath(MOD_ID, id),
                    entry.getValue().get());
            REGISTERED.put(id, event);
        }
        PENDING.clear();
    }

    /** Defer the registration to Fabric initialization while preserving existing .get() call sites. */
    private static Supplier<SoundEvent> registerEvent(String id, Supplier<SoundEvent> factory) {
        if (PENDING.putIfAbsent(id, factory) != null) {
            throw new IllegalArgumentException("Duplicate sound event: " + id);
        }
        return () -> {
            SoundEvent event = REGISTERED.get(id);
            if (event == null) {
                throw new IllegalStateException("Sound event not registered yet: " + id);
            }
            return event;
        };
    }

    private static Supplier<SoundEvent> register(String id) {
        return registerEvent(id, () -> SoundEvent.createVariableRangeEvent(
                Identifier.fromNamespaceAndPath(MOD_ID, id)));
    }
}

