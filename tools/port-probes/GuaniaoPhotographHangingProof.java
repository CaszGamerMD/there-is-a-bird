package EdDYON.guaniao.proof;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Verifies the real 26.2 APIs used by PhotographEntity without its unported photo-data dependencies. */
public abstract class GuaniaoPhotographHangingProof extends HangingEntity {
    protected GuaniaoPhotographHangingProof(EntityType<? extends GuaniaoPhotographHangingProof> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction direction) {
        return new AABB(pos);
    }

    @Override
    protected void recalculateBoundingBox() {
        super.recalculateBoundingBox();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, this.getDirection().get3DDataValue(), this.getPos());
    }

    @Override
    public Vec3 trackingPosition() {
        return Vec3.atLowerCornerOf(this.pos);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Item", ItemStack.CODEC, ItemStack.EMPTY);
        output.putByte("Facing", (byte) this.getDirection().get3DDataValue());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        ItemStack item = input.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (!item.isEmpty()) this.setDirection(Direction.from3DDataValue(input.getByteOr("Facing", (byte) 2)));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitLocation) {
        return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerable()) return false;
        this.dropItem(level, source.getEntity());
        this.kill(level);
        return true;
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        return true;
    }

    @Override
    public void dropItem(ServerLevel level, Entity breaker) {
        if (level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            this.spawnAtLocation(level, ItemStack.EMPTY);
        }
    }
}
