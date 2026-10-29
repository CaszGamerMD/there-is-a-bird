package EdDYON.guaniao.fabric;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/** Real Minecraft 26.2/Fabric API compilation probe for prank-food utilities. */
public final class GuaniaoPrankFoodProof {
    private GuaniaoPrankFoodProof() { }

    public static void callbacks() {
        UseItemCallback.EVENT.register((player, level, hand) -> InteractionResult.PASS);
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {});
    }

    public static ItemStack markedApple() {
        ItemStack stack = new ItemStack(Items.APPLE);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("GuaniaoPrankFood", true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static boolean isMarkedFood(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return stack.has(DataComponents.FOOD) && data != null
                && data.copyTag().getBoolean("GuaniaoPrankFood").orElse(false);
    }
}
