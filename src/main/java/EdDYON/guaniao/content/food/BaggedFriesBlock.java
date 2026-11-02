package EdDYON.guaniao.content.food;

import com.mojang.serialization.MapCodec;
import EdDYON.guaniao.registry.GuaniaoBlockEntityTypes;
import EdDYON.guaniao.registry.GuaniaoItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;


public final class BaggedFriesBlock extends BaseEntityBlock {
    public static final MapCodec<BaggedFriesBlock> CODEC = simpleCodec(BaggedFriesBlock::new);
    public static final int TOTAL_FRIES = 13;
    public static final IntegerProperty FRIES = IntegerProperty.create("fries", 0, TOTAL_FRIES);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 11.0D, 11.0D);

    public BaggedFriesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FRIES, TOTAL_FRIES));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hit) {
        return useFries(state, level, pos, player, hand);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        return useFries(state, level, pos, player, InteractionHand.MAIN_HAND);
    }

    private InteractionResult useFries(BlockState state, Level level, BlockPos pos, Player player,
                                       InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (player.getItemInHand(hand).is(GuaniaoItems.LAXATIVE.get())
                && level.getBlockEntity(pos) instanceof BaggedFriesBlockEntity fries
                && fries.hasRemainingFries()) {
            if (!level.isClientSide() && !fries.isLaxative()) {
                fries.setLaxative(true);
                if (!player.getAbilities().instabuild) {
                    player.getItemInHand(hand).shrink(1);
                }
                fries.syncToClient();
                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.65F, 0.8F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                if (level instanceof ServerLevel serverLevel) {
                    BaggedFriesBlockEntity.spawnPollutionBurst(serverLevel, pos);
                }
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }

        int remaining = state.getValue(FRIES);
        if (remaining <= 0) {
            if (!level.isClientSide()) {
                level.removeBlock(pos, false);
                level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.55F, 1.25F);
                level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }

        if (!player.canEat(false)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BaggedFriesBlockEntity fries) {
            boolean laxative = fries.isLaxative();
            int removed = fries.removeRandomFries(level.getRandom());
            if (removed > 0) {
                int nowRemaining = fries.getRemainingFries();
                level.setBlock(pos, state.setValue(FRIES, nowRemaining), Block.UPDATE_CLIENTS);
                fries.syncToClient();
                player.getFoodData().eat(removed, 0.12F);
                if (laxative) {
                    player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 12, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 6, 0));
                }
                player.awardStat(Stats.ITEM_USED.get(GuaniaoItems.BAGGED_FRIES.get()));
                level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS,
                        0.75F, 0.9F + level.getRandom().nextFloat() * 0.2F);
                level.gameEvent(player, GameEvent.EAT, pos);

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(GuaniaoItems.COOKED_FRIES.get())),
                            pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                            Math.max(2, removed * 2), 0.12D, 0.12D, 0.12D, 0.025D);
                }
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BaggedFriesBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != GuaniaoBlockEntityTypes.BAGGED_FRIES.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) -> {
            if (blockEntity instanceof BaggedFriesBlockEntity fries) {
                fries.tick(tickLevel, tickPos, tickState);
            }
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FRIES);
    }
}
