package EdDYON.guaniao.fabric;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/** Compile-only 26.2 test of death rewards, weapon Looting and item drop APIs. */
public final class GuaniaoDeathLootProof {
    private GuaniaoDeathLootProof() { }

    public static int lootingFromKillingWeapon(ServerLevel level, DamageSource damageSource) {
        if (damageSource.getEntity() instanceof LivingEntity killer) {
            var looting = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.LOOTING);
            return EnchantmentHelper.getItemEnchantmentLevel(looting, killer.getWeaponItem());
        }
        return 0;
    }

    public static void spawnReward(ServerLevel level, LivingEntity bird, int count) {
        if (count > 0) {
            bird.spawnAtLocation(level, new ItemStack(Items.GOLD_INGOT, count));
        }
    }
}
