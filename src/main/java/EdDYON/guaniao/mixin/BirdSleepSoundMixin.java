package EdDYON.guaniao.mixin;

import EdDYON.guaniao.event.BirdSleepSoundEvents;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The two 26.2 server broadcast paths replace Forge's AtPosition/AtEntity sound events. */
@Mixin(ServerLevel.class)
public abstract class BirdSleepSoundMixin {
    @Inject(
            method = "playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At("HEAD"), require = 1
    )
    private void guaniao$onSoundAtPosition(Entity except, double x, double y, double z,
                                            Holder<SoundEvent> sound, SoundSource source,
                                            float volume, float pitch, long seed, CallbackInfo ci) {
        BirdSleepSoundEvents.onSoundAtPosition((ServerLevel) (Object) this, new Vec3(x, y, z), volume);
    }

    @Inject(
            method = "playSeededSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At("HEAD"), require = 1
    )
    private void guaniao$onSoundAtEntity(Entity except, Entity sourceEntity,
                                          Holder<SoundEvent> sound, SoundSource source,
                                          float volume, float pitch, long seed, CallbackInfo ci) {
        BirdSleepSoundEvents.onSoundAtEntity((ServerLevel) (Object) this, sourceEntity, volume);
    }
}
