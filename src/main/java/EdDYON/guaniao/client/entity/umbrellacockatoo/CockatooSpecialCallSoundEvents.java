package EdDYON.guaniao.client.entity.umbrellacockatoo;

import EdDYON.guaniao.content.bird.umbrellacockatoo.UmbrellaCockatooEntity;
import EdDYON.guaniao.content.bird.umbrellacockatoo.CockatooSpecialCall;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class CockatooSpecialCallSoundEvents {
    private record Call(UmbrellaCockatooEntity bird, long startedAt, CockatooSpecialCallSound sound) { }
    private static final Map<UUID, Call> CALLS = new HashMap<>();

    private CockatooSpecialCallSoundEvents() { }

    public static void registerCallbacks() {
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            for (Entity entity : world.entitiesForRendering()) {
                if (entity instanceof UmbrellaCockatooEntity bird) onLivingTick(bird);
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> onClientTick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
    }

    private static void onLivingTick(UmbrellaCockatooEntity bird) {
        if (!bird.level().isClientSide() || !bird.isSpecialPerformanceActive()) return;
        Call previous = CALLS.get(bird.getUUID());
        if (previous != null && previous.bird() == bird && previous.startedAt() == bird.getSpecialCallStartTick()) return;
        if (previous != null) finish(previous);
        // Tracking a bird halfway through a call must not restart its thirteen-second recording.
        if (bird.getSpecialCallElapsedTicks() > CockatooSpecialCall.START_WINDOW_TICKS) return;
        CockatooSpecialCallSound sound = new CockatooSpecialCallSound(bird);
        CALLS.put(bird.getUUID(), new Call(bird, bird.getSpecialCallStartTick(), sound));
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    private static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        var iterator = CALLS.values().iterator();
        while (iterator.hasNext()) {
            Call call = iterator.next();
            if (call.bird().level() != minecraft.level || !call.sound().canPlaySound()) {
                finish(call);
                iterator.remove();
            } else if (call.bird().getSpecialCallElapsedTicks() > 2
                    && !minecraft.getSoundManager().isActive(call.sound())) {
                // Sound-engine reloads retire active sounds. Keep the call marker
                // until the performance ends so the 13-second take never replays.
                call.sound().finish();
            }
        }
    }

    private static void finish(Call call) {
        call.sound().finish();
        Minecraft.getInstance().getSoundManager().stop(call.sound());
    }

    private static void stopSounds() {
        for (Call call : CALLS.values()) finish(call);
    }

    private static void clear() {
        stopSounds();
        CALLS.clear();
    }

}
