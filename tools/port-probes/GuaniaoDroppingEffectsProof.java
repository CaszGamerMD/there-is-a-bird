package EdDYON.guaniao.fabric;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Standalone, compile-only check for the modern 26.2 effects/dust API. */
public final class GuaniaoDroppingEffectsProof {
    private GuaniaoDroppingEffectsProof() { }

    public static void probe() {
        new MobEffectInstance(MobEffects.NAUSEA, 160, 0);
        new MobEffectInstance(MobEffects.HUNGER, 160, 0);
        new MobEffectInstance(MobEffects.SLOWNESS, 60, 0);
        new MobEffectInstance(MobEffects.SPEED, 55, 0);
        new MobEffectInstance(MobEffects.WEAKNESS, 70, 0);
        new DustParticleOptions(ARGB.colorFromFloat(1.0F, 0.25F, 0.16F, 0.06F), 0.92F);
        new DustParticleOptions(ARGB.colorFromFloat(1.0F, 0.18F, 0.42F, 0.08F), 0.78F);
    }
}
