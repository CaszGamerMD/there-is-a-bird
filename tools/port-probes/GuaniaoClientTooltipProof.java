package EdDYON.guaniao.fabric;

import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Compile-only test for the Minecraft 26.2 client-side tooltip and Shift APIs. */
public final class GuaniaoClientTooltipProof implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) ->
                addTip(stack, lines, Minecraft.getInstance().hasShiftDown()));
    }

    private static void addTip(ItemStack stack, List<Component> lines, boolean shift) {
        if (shift) {
            lines.add(Component.literal("Controls").withStyle(ChatFormatting.GRAY));
        }
    }
}
