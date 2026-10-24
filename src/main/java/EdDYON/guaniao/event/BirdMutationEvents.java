package EdDYON.guaniao.event;

import EdDYON.guaniao.content.bird.mutation.BirdMutation;
import EdDYON.guaniao.content.bird.mutation.BirdMutationHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Original golden-bird death reward, now invoked from the Fabric death-loot mixin.
 * Never add to the normal loot list: other bird species intentionally drop none.
 */
public final class BirdMutationEvents {
    private BirdMutationEvents() { }

    public static void dropMutationGold(LivingEntity entity, ServerLevel level, DamageSource source) {
        if (!(entity instanceof BirdMutationHolder holder)) {
            return;
        }
        BirdMutation mutation = holder.getBirdMutation();
        if (!mutation.isGold()) {
            return;
        }

        int count = mutation == BirdMutation.GOLDEN_PURE ? 3 : 1;
        int lootingLevel = 0;
        if (source.getEntity() instanceof LivingEntity killer) {
            var looting = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.LOOTING);
            lootingLevel = EnchantmentHelper.getItemEnchantmentLevel(looting, killer.getWeaponItem());
        }
        if (lootingLevel > 0 && level.getRandom().nextFloat() < 0.5F * lootingLevel) {
            count++;
        }

        entity.spawnAtLocation(level, new ItemStack(Items.GOLD_INGOT, count));
    }
}
