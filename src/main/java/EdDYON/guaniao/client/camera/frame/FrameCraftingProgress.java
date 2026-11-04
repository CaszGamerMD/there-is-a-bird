package EdDYON.guaniao.client.camera.frame;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/** Compatibility facade for the 26.2 extracted progress-meadow presentation. */
final class FrameCraftingProgress {
    private FrameCraftingProgress() { }

    static void extract(GuiGraphicsExtractor graphics, ItemStack food, float eatingTicks) {
        FrameCraftingProgress2D.extract(graphics, food, eatingTicks);
    }
}
