package EdDYON.guaniao.proof;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Compile the real 26.2 block interactions and lossless variant codec independent of the unported bath BE. */
public final class GuaniaoBirdBathBlockProof extends BaseEntityBlock {
    private enum Variant implements StringRepresentable {
        WOOD("wooden_bird_bath"), STONE("stone_bird_bath");

        static final Codec<Variant> CODEC = StringRepresentable.fromEnum(Variant::values);
        private final String id;

        Variant(String id) { this.id = id; }

        @Override
        public String getSerializedName() { return id; }
    }

    public static final MapCodec<GuaniaoBirdBathBlockProof> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Variant.CODEC.fieldOf("variant").forGetter(GuaniaoBirdBathBlockProof::variant),
            propertiesCodec()
    ).apply(instance, GuaniaoBirdBathBlockProof::new));

    private final Variant variant;

    public GuaniaoBirdBathBlockProof(Variant variant, Properties properties) {
        super(properties);
        this.variant = variant;
    }

    private Variant variant() { return variant; }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return null; }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        return held.isEmpty() ? InteractionResult.PASS : sidedSuccess(level);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        return sidedSuccess(level);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        return super.playerWillDestroy(level, pos, state, player);
    }

    private static InteractionResult sidedSuccess(Level level) {
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }
}
