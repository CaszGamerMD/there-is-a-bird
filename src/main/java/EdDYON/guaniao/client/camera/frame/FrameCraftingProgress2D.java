package EdDYON.guaniao.client.camera.frame;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;

/**
 * The original 2D grass, visible food, bite fragments and fade from
 * FrameCraftingProgress, submitted with 26.2 GUI extraction.
 *
 * The INDEPENDENT 3D progress bird still needs its GeckoLib 5 PiP
 * registration; do not replace it with a 2D icon.
 */
final class FrameCraftingProgress2D {
    private FrameCraftingProgress2D() { }

    static void extract(GuiGraphicsExtractor graphics,ItemStack food,float eatingTicks) {
        FrameCraftingMeadow.render(graphics);
        float foodScale=eatingTicks<0 ? 1 : 1-Mth.clamp((eatingTicks-8)/16,0,1);
        if(foodScale>0 && !food.isEmpty()) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(315,261);
            graphics.pose().scale(0.7F*foodScale,0.7F*foodScale);
            graphics.item(food,-8,-12);
            graphics.pose().popMatrix();
        }
        if(eatingTicks>=6 && eatingTicks<22) {
            float bite=(eatingTicks-6)/16;
            int spread=Math.round(bite*6),lift=Math.round((float)Math.sin(bite*Math.PI)*4);
            graphics.fill(312-spread,258-lift,314-spread,260-lift,0xFFC7B783);
            graphics.fill(316+spread,259-lift,317+spread,260-lift,0xFFE2D5AE);
        }
    }
}
