package EdDYON.guaniao.fabric;

import net.minecraft.client.Minecraft;

/** Native Minecraft 26.2 compile proof for F1 HUD and blocking overlays. */
public final class GuaniaoCameraHudVisibilityProof {
    private GuaniaoCameraHudVisibilityProof() { }

    public static boolean hidden(Minecraft minecraft) {
        return minecraft.gui.hud.isHidden();
    }

    public static void restore(Minecraft minecraft, boolean hidden) {
        if (minecraft.gui.hud.isHidden() != hidden) {
            minecraft.gui.hud.toggle();
        }
    }

    public static boolean blockedByOverlay(Minecraft minecraft) {
        return minecraft.gui.overlay() != null;
    }
}
