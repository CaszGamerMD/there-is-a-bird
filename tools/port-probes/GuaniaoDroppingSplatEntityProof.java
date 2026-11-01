package EdDYON.guaniao.fabric;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/** Actual 26.2 splat damage, interaction, item-drop and typed-save API compilation gate. */
public final class GuaniaoDroppingSplatEntityProof extends Entity implements GeoEntity {
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public GuaniaoDroppingSplatEntityProof(EntityType<?> type, Level level) { super(type, level); }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        output.putFloat("LocalX", 0.25F);
        output.store("AttachedEntity", UUIDUtil.CODEC, UUID.randomUUID());
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        input.getFloatOr("LocalX", 0.0F);
        input.getInt("MaxAge").isPresent();
        input.read("AttachedEntity", UUIDUtil.CODEC).orElse(null);
    }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return !isInvulnerable();
    }
    @Override public boolean hurtClient(DamageSource source) { return true; }
    @Override public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitLocation) {
        return level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }
    @Override public float getPickRadius() { return 0.25F; }
    public ItemEntity drop(ServerLevel level, ItemStack item, Player player, InteractionHand hand) {
        item.hurtAndBreak(1, player, hand);
        return spawnAtLocation(level, item);
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }
}
