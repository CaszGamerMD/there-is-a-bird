package EdDYON.guaniao.fabric;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Compile-only probe for Minecraft Fabric 26.2 HUD extraction registration. */
public final class GuaniaoCameraHudRegistryProof {
    private GuaniaoCameraHudRegistryProof() { }

    public static void register(Identifier id) {
        HudElementRegistry.addLast(id, (GuiGraphicsExtractor graphics, deltaTracker) -> {
            // A native extractor can submit the authored bezel after a real
            // filtered lens has been supplied by the preview renderer.
            graphics.fill(0, 0, 1, 1, 0xFFFFFFFF);
        });
    }
}
