package EdDYON.guaniao.event;

import EdDYON.guaniao.content.bird.BirdTags;
import EdDYON.guaniao.content.fan.FeatherFanProjectileEntity;
import EdDYON.guaniao.registry.GuaniaoItems;
import EdDYON.guaniao.registry.GuaniaoParticleTypes;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Feather-fan attack exemptions and death effects. Server-side callbacks are
 * the Fabric equivalents of the original Forge LivingAttack/Death handlers.
 */
public final class FeatherFanCombatEvents {
    private FeatherFanCombatEvents() {
    }

    public static void registerCallbacks() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(FeatherFanCombatEvents::allowDamage);
        ServerLivingEntityEvents.AFTER_DEATH.register(FeatherFanCombatEvents::afterDeath);
    }

    /** Return false only for feather-fan damage to a bird. */
    public static boolean allowDamage(LivingEntity victim, DamageSource source, float amount) {
        if (!victim.getType().is(BirdTags.BIRDS)) {
            return true;
        }

        Entity directAttacker = source.getDirectEntity();
        boolean projectileAttack = directAttacker instanceof FeatherFanProjectileEntity;
        boolean meleeAttack = source.getEntity() instanceof Player player
                && directAttacker == player
                && player.getMainHandItem().is(GuaniaoItems.WIND_FEATHER_FAN.get());
        if (projectileAttack || meleeAttack) {
            return false;
        }
        return true;
    }

    /** Preserve particles and original random count only on lethal fan attacks. */
    public static void afterDeath(LivingEntity victim, DamageSource source) {
        if (!(victim.level() instanceof ServerLevel level) || victim instanceof Player) {
            return;
        }

        Entity directAttacker = source.getDirectEntity();
        boolean projectileKill = directAttacker instanceof FeatherFanProjectileEntity;
        boolean meleeKill = source.getEntity() instanceof Player player
                && directAttacker == player
                && player.getMainHandItem().is(GuaniaoItems.WIND_FEATHER_FAN.get());
        if (!projectileKill && !meleeKill) {
            return;
        }

        double x = victim.getX();
        double y = victim.getY() + victim.getBbHeight() * 0.72D;
        double z = victim.getZ();
        level.sendParticles(ParticleTypes.POOF, x, y, z, 3, 0.16D, 0.18D, 0.16D, 0.018D);
        int featherCount = 3 + level.getRandom().nextInt(5);
        level.sendParticles(GuaniaoParticleTypes.KILL_FEATHER.get(), x, y + 0.10D, z,
                featherCount, 0.22D, 0.10D, 0.22D, 0.0D);
    }
}
