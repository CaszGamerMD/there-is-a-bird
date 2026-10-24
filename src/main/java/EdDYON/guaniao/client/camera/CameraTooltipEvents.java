package EdDYON.guaniao.client.camera;

import EdDYON.guaniao.registry.GuaniaoItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import net.minecraft.network.chat.Component;

public final class CameraTooltipEvents {
    private CameraTooltipEvents() {
    }

    public static void registerCallbacks() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) ->
                appendTooltip(stack, lines, Minecraft.getInstance().hasShiftDown()));
    }

    static void appendTooltip(ItemStack stack, List<Component> lines, boolean hasShiftDown) {
        if (!stack.is(GuaniaoItems.NIKON_D750.get())) return;
        if (hasShiftDown) {
            lines.add(Component.translatable("tooltip.guaniao.camera.open").withStyle(ChatFormatting.GRAY));
            for (Component line : CameraHelpScreen.controlLines()) {
                lines.add(line.copy().withStyle(ChatFormatting.GRAY));
            }
        } else {
            lines.add(Component.translatable("tooltip.guaniao.camera.help",
                    Component.translatable("key.keyboard.left.shift")).withStyle(ChatFormatting.GRAY));
        }
    }
}
