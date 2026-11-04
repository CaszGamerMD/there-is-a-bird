package EdDYON.guaniao.fabric;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

/** Compile-only proof that Fabric 26.2 exposes a drawing-phase END_MAIN hook. */
public final class GuaniaoCameraLevelRenderProof {
    private GuaniaoCameraLevelRenderProof() { }

    public static void register() {
        LevelRenderEvents.END_MAIN.register(context -> {
            // GPU work belongs in the draw phase, never GUI extraction.
        });
    }
}
