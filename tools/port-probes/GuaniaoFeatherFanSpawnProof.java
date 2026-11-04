package EdDYON.guaniao.proof;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Compile the 26.2 vanilla projectile spawn-packet path without the unported fan combat logic. */
public abstract class GuaniaoFeatherFanSpawnProof extends ThrowableItemProjectile {
    protected GuaniaoFeatherFanSpawnProof(EntityType<? extends GuaniaoFeatherFanSpawnProof> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FEATHER;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    // Modern owner-initialized projectiles require their initial item stack.
    protected GuaniaoFeatherFanSpawnProof(EntityType<? extends GuaniaoFeatherFanSpawnProof> type,
                                          net.minecraft.world.entity.LivingEntity owner, Level level) {
        super(type, owner, level, new ItemStack(Items.FEATHER));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return super.getAddEntityPacket(serverEntity);
    }
}
