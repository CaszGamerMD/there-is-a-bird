package EdDYON.guaniao.proof;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

/** Verify plain-water versus effect-bearing potion bottles against the 26.2 item component API. */
public final class GuaniaoBirdBathWaterBottleProof {
    private GuaniaoBirdBathWaterBottleProof() {
    }

    public static boolean isWaterBottle(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return stack.is(Items.POTION) && contents != null && contents.is(Potions.WATER)
                && contents.customEffects().isEmpty();
    }
}
