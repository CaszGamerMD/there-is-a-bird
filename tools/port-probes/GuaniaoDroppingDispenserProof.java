package EdDYON.guaniao.proof;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

/** Compile the 26.2 dispenser contract without the still-unported dropping item class. */
public final class GuaniaoDroppingDispenserProof extends DefaultDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        ServerLevel level = source.level();
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Position position = DispenserBlock.getDispensePosition(source);
        stack.shrink(1);
        return stack;
    }

    private static void shootSameAsOriginal(Projectile projectile, ServerLevel level, Direction direction) {
        projectile.shoot(direction.getStepX(), direction.getStepY() + 0.1D,
                direction.getStepZ(), 1.25F, 6.0F);
        level.addFreshEntity(projectile);
    }
}
