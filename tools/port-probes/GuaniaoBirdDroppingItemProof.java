package EdDYON.guaniao.proof;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Compile the 26.2 throwing, inventory, cooldown and fertilizing contracts in isolation. */
public final class GuaniaoBirdDroppingItemProof extends Item {
    public GuaniaoBirdDroppingItemProof(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return context.getLevel().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int chargeTicks = this.getUseDuration(stack, entity) - timeLeft;
        if (entity instanceof Player player && chargeTicks >= 3) {
            player.getCooldowns().addCooldown(stack, 10);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
    }

    private static void shootOriginalTrajectory(Projectile projectile, Player player, float speed) {
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, speed, 0.7F);
    }

    private static boolean fertilize(ServerLevel level, BonemealableBlock crop, BlockPos pos, BlockState state) {
        if (!crop.isValidBonemealTarget(level, pos, state)) {
            return false;
        }
        if (!crop.isBonemealSuccess(level, level.getRandom(), pos, state)) {
            return false;
        }
        crop.performBonemeal(level, level.getRandom(), pos, state);
        return true;
    }
}
