package EdDYON.guaniao.proof;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Compile the 26.2 breadcrumb right-click, placement, world border and particle API. */
public final class GuaniaoBreadcrumbItemProof extends Item {
    public GuaniaoBreadcrumbItemProof(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            BlockPos pos = BlockPos.containing(hit.getLocation());
            if (!serverLevel.mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, stack)) {
                return InteractionResult.PASS;
            }
        }
        player.swing(hand, true);
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);
        if (state.canBeReplaced(placeContext)) {
            Vec3 point = context.getClickLocation();
            return point != null ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    private static boolean canScatterAt(Level level, BlockPos pos) {
        return level.hasChunkAt(pos) && !level.isOutsideBuildHeight(pos)
                && level.getWorldBorder().isWithinBounds(pos)
                && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getFluidState(pos).isEmpty();
    }

    private static void throwTrail(ServerLevel level, BlockPos pos, BlockState state, Block block, ItemStack stack) {
        RandomSource random = level.getRandom();
        ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, stack.getItem());
        level.sendParticles(particle, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                12 + random.nextInt(7), 0.34, 0.08, 0.34, 0.02);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        level.scheduleTick(pos, block, 600);
    }
}
