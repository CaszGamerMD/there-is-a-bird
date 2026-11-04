package EdDYON.guaniao.content.fan;

import EdDYON.guaniao.content.enchantment.GuaniaoEnchantments;
import net.minecraft.world.item.ItemStack;

public final class FeatherFanEnchantments {
    private FeatherFanEnchantments() {
    }

    public static boolean hasBurialPlume(ItemStack stack) {
        return GuaniaoEnchantments.has(stack, GuaniaoEnchantments.BURIAL_PLUME);
    }

    public static boolean hasRivenPlume(ItemStack stack) {
        return GuaniaoEnchantments.has(stack, GuaniaoEnchantments.RIVEN_PLUME);
    }

    public static boolean hasHuntingReturn(ItemStack stack) {
        return GuaniaoEnchantments.has(stack, GuaniaoEnchantments.HUNTING_RETURN);
    }
}
