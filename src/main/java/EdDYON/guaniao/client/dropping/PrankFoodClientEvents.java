package EdDYON.guaniao.client.dropping;

import EdDYON.guaniao.content.dropping.BirdDroppingMessageUtil;
import EdDYON.guaniao.content.dropping.PrankFoodUtil;
import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Client-only Fabric callback for original prank-food and bird-dropping tooltips. */
public final class PrankFoodClientEvents {
    private PrankFoodClientEvents() { }

    public static void registerCallbacks() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) ->
                appendTooltip(stack, lines, Minecraft.getInstance().hasShiftDown()));
    }

    static void appendTooltip(ItemStack stack, List<Component> lines, boolean showDetails) {
        if (PrankFoodUtil.isPrankFood(stack)) {
            if (!lines.isEmpty()) {
                lines.set(0, PrankFoodUtil.storedPrankDisplayName(stack, lines.get(0)));
            }
            if (showDetails) {
                lines.add(BirdDroppingMessageUtil.stableTooltip(
                        "tooltip.guaniao.prank_food_suspicious", stack).copy().withStyle(ChatFormatting.GRAY));
            }
        }
        if (PrankFoodUtil.isDropping(stack)) {
            lines.add(Component.translatable("tooltip.guaniao.bird_dropping_fertilizer")
                    .withStyle(ChatFormatting.GRAY));
            if (showDetails) {
                lines.add(BirdDroppingMessageUtil.stableTooltip(
                        "tooltip.guaniao.dropping_evidence", stack).copy().withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
