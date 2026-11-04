package EdDYON.guaniao.content.dropping;

import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BirdDroppingItem extends Item {
    private static final Identifier EVIDENCE_COLLECTOR_ADVANCEMENT = Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "husbandry/evidence_collector");
    private static final int USE_DURATION_TICKS = 72000;
    private static final int MIN_THROW_CHARGE_TICKS = 3;
    private static final float MIN_THROW_SPEED = 0.45F;
    private static final float MAX_THROW_SPEED = 1.75F;
    private final BirdDroppingVariant variant;

    public BirdDroppingItem(BirdDroppingVariant variant, Properties properties) {
        super(properties);
        this.variant = variant;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState clickedState = level.getBlockState(context.getClickedPos());
        if (clickedState.getBlock() instanceof FarmlandBlock) {
            return this.fertilizeAroundFarmland(context);
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        int chargeTicks = this.getUseDuration(stack, entity) - timeLeft;
        if (chargeTicks < MIN_THROW_CHARGE_TICKS) {
            return false;
        }

        float power = getThrowPower(chargeTicks);
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.45F + power * 0.15F, 0.72F + level.getRandom().nextFloat() * 0.22F);

            BirdDroppingProjectileEntity projectile = new BirdDroppingProjectileEntity(level, player, this.variant);
            ItemStack renderStack = stack.copy();
            renderStack.setCount(1);
            projectile.setItem(renderStack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, MIN_THROW_SPEED + power * (MAX_THROW_SPEED - MIN_THROW_SPEED), 0.7F);
            level.addFreshEntity(projectile);

            player.awardStat(Stats.ITEM_USED.get(this));
            player.getCooldowns().addCooldown(stack, 10);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (entity instanceof ServerPlayer player) {
            BirdDroppingMessageUtil.grant(player, EVIDENCE_COLLECTOR_ADVANCEMENT, "has_dropping");
        }
    }

    public BirdDroppingVariant variant() {
        return this.variant;
    }

    private InteractionResult fertilizeAroundFarmland(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos center = context.getClickedPos();
        List<BlockPos> targets = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos cropPos = center.offset(x, 1, z);
                BlockState cropState = level.getBlockState(cropPos);
                if (isValidFertilizeTarget(level, cropPos, cropState)) {
                    targets.add(cropPos.immutable());
                }
            }
        }

        if (targets.isEmpty()) {
            return InteractionResult.SUCCESS;
        }

        boolean grewAny = false;
        float chance = BirdDroppingUtil.farmlandFertilizeChance(this.variant);
        for (BlockPos cropPos : targets) {
            BlockState cropState = level.getBlockState(cropPos);
            if (!isValidFertilizeTarget(level, cropPos, cropState) || level.getRandom().nextFloat() >= chance) {
                continue;
            }
            BonemealableBlock crop = (BonemealableBlock)cropState.getBlock();
            if (crop.isBonemealSuccess(level, level.getRandom(), cropPos, cropState)) {
                crop.performBonemeal(serverLevel, level.getRandom(), cropPos, cropState);
                level.levelEvent(1505, cropPos, 0);
                grewAny = true;
            }
        }

        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (grewAny) {
            level.playSound(null, center, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.BLOCKS, 0.8F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        } else {
            level.playSound(null, center, SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 0.55F, 0.8F + level.getRandom().nextFloat() * 0.2F);
            serverLevel.sendParticles(ParticleTypes.POOF, center.getX() + 0.5D, center.getY() + 1.05D, center.getZ() + 0.5D, 4, 0.35D, 0.08D, 0.35D, 0.01D);
        }

        return InteractionResult.SUCCESS;
    }

    private static boolean isValidFertilizeTarget(Level level, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof BonemealableBlock crop && crop.isValidBonemealTarget(level, pos, state);
    }

    private static float getThrowPower(int chargeTicks) {
        float power = chargeTicks / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        return Math.min(power, 1.0F);
    }
}
