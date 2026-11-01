package EdDYON.guaniao.fabric;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** Compile-only proof of the Minecraft 26.2 Entity subclass and persistence contracts. */
public final class GuaniaoEarthwormEntityProof extends Entity {
    private static final EntityDataAccessor<Integer> BURROW = SynchedEntityData.defineId(
            GuaniaoEarthwormEntityProof.class, EntityDataSerializers.INT);

    public GuaniaoEarthwormEntityProof(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BURROW, 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putDouble("HomeX", 1.0D);
        output.putInt("SoilAge", 120);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        double x = input.getDoubleOr("HomeX", 0.0D);
        if (input.getDouble("HomeX").isPresent() && x > 0.0D) {
            this.entityData.set(BURROW, input.getIntOr("Burrow", 0));
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitLocation) {
        return level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override public boolean isPickable() { return this.isAlive(); }
    @Override public float getPickRadius() { return 0.08F; }

    public static <T extends Entity> T spawn(EntityType<T> type, ServerLevel level) {
        return type.create(level, EntitySpawnReason.TRIGGERED);
    }
}
