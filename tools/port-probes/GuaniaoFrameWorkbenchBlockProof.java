import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

/** Compile-only proof for the 26.2 block use/removal hooks and push-reaction setting. */
public final class GuaniaoFrameWorkbenchBlockProof extends BaseEntityBlock {
    public static final MapCodec<GuaniaoFrameWorkbenchBlockProof> CODEC =
            simpleCodec(GuaniaoFrameWorkbenchBlockProof::new);

    public GuaniaoFrameWorkbenchBlockProof(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level,
                                          BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    public static Properties workbenchSettings() {
        return Properties.of().pushReaction(PushReaction.BLOCK);
    }
}
