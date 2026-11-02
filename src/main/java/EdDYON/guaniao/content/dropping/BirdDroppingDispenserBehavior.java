package EdDYON.guaniao.content.dropping;

import EdDYON.guaniao.registry.GuaniaoEntityTypes;
import EdDYON.guaniao.registry.GuaniaoItems;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public final class BirdDroppingDispenserBehavior extends DefaultDispenseItemBehavior {
    private final BirdDroppingVariant variant;

    private BirdDroppingDispenserBehavior(BirdDroppingVariant variant) {
        this.variant = variant;
    }

    public static void registerAll() {
        DispenserBlock.registerBehavior(GuaniaoItems.BIRD_DROPPING_1.get(), new BirdDroppingDispenserBehavior(BirdDroppingVariant.ONE));
        DispenserBlock.registerBehavior(GuaniaoItems.BIRD_DROPPING_2.get(), new BirdDroppingDispenserBehavior(BirdDroppingVariant.TWO));
        DispenserBlock.registerBehavior(GuaniaoItems.BIRD_DROPPING_3.get(), new BirdDroppingDispenserBehavior(BirdDroppingVariant.THREE));
        DispenserBlock.registerBehavior(GuaniaoItems.BIRD_DROPPING_4.get(), new BirdDroppingDispenserBehavior(BirdDroppingVariant.FOUR));
    }

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        ServerLevel level = source.level();
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Position position = DispenserBlock.getDispensePosition(source);
        BirdDroppingProjectileEntity projectile = getProjectile(level, position, stack);
        // Preserve the original 1.25 projectile speed and six-degree scatter.
        projectile.shoot(direction.getStepX(), direction.getStepY() + 0.1D,
                direction.getStepZ(), 1.25F, 6.0F);
        level.addFreshEntity(projectile);
        stack.shrink(1);
        return stack;
    }

    private BirdDroppingProjectileEntity getProjectile(Level level, Position position, ItemStack stack) {
        BirdDroppingProjectileEntity projectile = new BirdDroppingProjectileEntity(GuaniaoEntityTypes.BIRD_DROPPING_PROJECTILE.get(), level);
        projectile.setPos(position.x(), position.y(), position.z());
        projectile.setVariant(this.variant);
        ItemStack renderedStack = stack.copy();
        renderedStack.setCount(1);
        projectile.setItem(renderedStack);
        return projectile;
    }

}
