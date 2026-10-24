package EdDYON.guaniao.mixin;

import EdDYON.guaniao.event.BirdFeatherEvents;
import EdDYON.guaniao.event.BirdMutationEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Faithful Fabric replacement for Forge's low-priority LivingDropsEvent:
 * no ordinary item drops from birds, rare golden mutation rewards remain,
 * and vanilla experience is still awarded once.
 */
@Mixin(LivingEntity.class)
public abstract class BirdDeathDropsMixin {
    @Shadow
    protected abstract void dropExperience(ServerLevel level, @Nullable Entity killer);

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    private void guaniao$replaceBirdDeathDrops(ServerLevel level, DamageSource source, CallbackInfo ci) {
        LivingEntity bird = (LivingEntity) (Object) this;
        if (!BirdFeatherEvents.suppressDeathDrops(bird)) {
            return;
        }
        BirdMutationEvents.dropMutationGold(bird, level, source);
        this.dropExperience(level, source.getEntity());
        ci.cancel();
    }
}
