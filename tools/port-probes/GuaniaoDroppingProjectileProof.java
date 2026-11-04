package EdDYON.guaniao.fabric;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/** Compile-only proof for 26.2 thrown items, typed saves, and GeckoLib 5 entity caching. */
public final class GuaniaoDroppingProjectileProof extends ThrowableItemProjectile implements GeoEntity {
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public GuaniaoDroppingProjectileProof(EntityType<? extends GuaniaoDroppingProjectileProof> type, Level level) {
        super(type, level);
    }

    public GuaniaoDroppingProjectileProof(EntityType<? extends GuaniaoDroppingProjectileProof> type,
                                          LivingEntity owner, Level level) {
        super(type, owner, level, new ItemStack(Items.SNOWBALL));
    }

    @Override protected Item getDefaultItem() { return Items.SNOWBALL; }
    @Override protected double getDefaultGravity() { return 0.06D; }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("NaturalDropping", true);
        output.putLong("StripStartedAt", 5L);
        output.store("SourceBirdUuid", UUIDUtil.CODEC, UUID.randomUUID());
    }

    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getBooleanOr("NaturalDropping", false);
        input.getLongOr("StripStartedAt", 0L);
        input.read("SourceBirdUuid", UUIDUtil.CODEC).orElse(null);
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }
}
