package EdDYON.guaniao.mixin;

import EdDYON.guaniao.content.dropping.PrankFoodEffectUtil;
import EdDYON.guaniao.content.dropping.PrankFoodUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restore the server-side prank effect when eating finishes.
 * Guard against a swapped item; Minecraft 26.2 verifies stack identity
 * before finishing a consumable use.
 */
@Mixin(LivingEntity.class)
public abstract class PrankFoodFinishMixin {
    @Inject(method = "completeUsingItem", at = @At("HEAD"), require = 1)
    private void guaniao$onFinishedPrankFood(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide() || !entity.isUsingItem()) {
            return;
        }
        ItemStack active = entity.getUseItem();
        if (!active.isEmpty() && PrankFoodUtil.isPrankFood(active)
                && active == entity.getItemInHand(entity.getUsedItemHand())) {
            PrankFoodEffectUtil.apply(entity);
        }
    }
}
