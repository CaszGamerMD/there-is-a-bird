package EdDYON.guaniao.event;

import EdDYON.guaniao.content.dropping.PrankFoodUtil;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

/** Restore the original prank-food use behaviour with Fabric item callbacks. */
public final class PrankFoodEvents {
    private PrankFoodEvents() { }

    public static void registerCallbacks() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!PrankFoodUtil.isPrankFood(stack) || !stack.has(DataComponents.FOOD)
                    || player.isUsingItem()) {
                return InteractionResult.PASS;
            }
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        });
    }
}
