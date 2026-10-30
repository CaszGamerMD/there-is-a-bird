package EdDYON.guaniao.client.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public final class PhotographClientActions {
    private PhotographClientActions() {
    }

    public static void openScreen(ItemStack stack) {
        Minecraft.getInstance().gui.setScreen(new PhotographScreen(stack));
    }
}
