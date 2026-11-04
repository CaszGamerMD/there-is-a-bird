package EdDYON.guaniao.fabric;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

/** Compile-only test for 26.2 builder-based entity data initialization. */
public abstract class GuaniaoBirdSyncedDataProof extends Animal {
    private static final EntityDataAccessor<Integer> BIRD_STATE =
            SynchedEntityData.defineId(GuaniaoBirdSyncedDataProof.class, EntityDataSerializers.INT);

    protected GuaniaoBirdSyncedDataProof(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BIRD_STATE, 0);
    }
}
