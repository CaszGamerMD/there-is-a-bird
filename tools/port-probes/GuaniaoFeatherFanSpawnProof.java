package EdDYON.guaniao.proof;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return super.getAddEntityPacket(serverEntity);
    }
}
