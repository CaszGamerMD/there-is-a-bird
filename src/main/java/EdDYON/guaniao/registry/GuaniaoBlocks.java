package EdDYON.guaniao.registry;

import EdDYON.guaniao.content.bath.BirdBathBlock;
import EdDYON.guaniao.content.bath.BirdBathVariant;
import EdDYON.guaniao.content.dropping.BirdDroppingStainBlock;
import EdDYON.guaniao.content.feed.BreadcrumbPileBlock;
import EdDYON.guaniao.content.food.BaggedFriesBlock;
import EdDYON.guaniao.content.cage.BirdCageBlock;
import EdDYON.guaniao.content.cage.BirdCageVariant;
import EdDYON.guaniao.content.nest.CrowNestBlock;
import EdDYON.guaniao.content.camera.frame.FrameWorkbenchBlock;
import EdDYON.guaniao.fabric.FabricRegistryQueue;
import EdDYON.guaniao.fabric.GuaniaoRegistryIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class GuaniaoBlocks {
    public static final FabricRegistryQueue<Block> BLOCKS = new FabricRegistryQueue<>(BuiltInRegistries.BLOCK, "guaniao");
    public static final FabricRegistryQueue.Ref<Block> FRAME_WORKBENCH = BLOCKS.register("frame_workbench", () ->
            new FrameWorkbenchBlock(BlockBehaviour.Properties.of().setId(GuaniaoRegistryIds.block("frame_workbench")).strength(2.5F).sound(SoundType.WOOD).noOcclusion()));
    public static final FabricRegistryQueue.Ref<Block> BREADCRUMBS = BLOCKS.register("breadcrumbs", () ->
            new BreadcrumbPileBlock(BlockBehaviour.Properties.of().setId(GuaniaoRegistryIds.block("breadcrumbs"))
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.SAND)
                    .randomTicks()));
    public static final FabricRegistryQueue.Ref<Block> BAGGED_FRIES = BLOCKS.register("bagged_fries", () ->
            new BaggedFriesBlock(BlockBehaviour.Properties.of().setId(GuaniaoRegistryIds.block("bagged_fries"))
                    .strength(0.2F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));
    public static final FabricRegistryQueue.Ref<Block> SMALL_BIRD_CAGE = registerBirdCage(BirdCageVariant.SMALL);
    public static final FabricRegistryQueue.Ref<Block> MEDIUM_BIRD_CAGE = registerBirdCage(BirdCageVariant.MEDIUM);
    public static final FabricRegistryQueue.Ref<Block> LARGE_BIRD_CAGE = registerBirdCage(BirdCageVariant.LARGE);
    public static final FabricRegistryQueue.Ref<Block> WOODEN_BIRD_BATH = registerBirdBath(BirdBathVariant.WOODEN_BIRD_BATH);
    public static final FabricRegistryQueue.Ref<Block> STONE_BIRD_BATH = registerBirdBath(BirdBathVariant.STONE_BIRD_BATH);
    public static final FabricRegistryQueue.Ref<Block> BIRD_BATH = registerBirdBath(BirdBathVariant.BIRD_BATH);
    public static final FabricRegistryQueue.Ref<Block> WOODEN_BIRD_BATH_2 = registerBirdBath(BirdBathVariant.WOODEN_BIRD_BATH_2);
    public static final FabricRegistryQueue.Ref<Block> STONE_BIRD_BATH_2 = registerBirdBath(BirdBathVariant.STONE_BIRD_BATH_2);
    public static final FabricRegistryQueue.Ref<Block> BIRD_BATH_2 = registerBirdBath(BirdBathVariant.BIRD_BATH_2);
    public static final FabricRegistryQueue.Ref<Block> CROW_NEST = BLOCKS.register("crow_nest", () ->
            new CrowNestBlock(BlockBehaviour.Properties.of().setId(GuaniaoRegistryIds.block("crow_nest"))
                    .strength(0.7F)
                    .sound(SoundType.GRASS)
                    .noOcclusion()));
    public static final FabricRegistryQueue.Ref<Block> BIRD_DROPPING_STAIN_LIGHT = BLOCKS.register("bird_dropping_stain_light", () ->
            new BirdDroppingStainBlock(BlockBehaviour.Properties.of().setId(GuaniaoRegistryIds.block("bird_dropping_stain_light"))
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.SLIME_BLOCK)
                    .randomTicks()
                    .noOcclusion()));
    public static final FabricRegistryQueue.Ref<Block> BIRD_DROPPING_STAIN_DARK = BLOCKS.register("bird_dropping_stain_dark", () ->
            new BirdDroppingStainBlock(BlockBehaviour.Properties.of().setId(GuaniaoRegistryIds.block("bird_dropping_stain_dark"))
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.SLIME_BLOCK)
                    .randomTicks()
                    .noOcclusion()));

    private GuaniaoBlocks() {
    }

    private static FabricRegistryQueue.Ref<Block> registerBirdCage(BirdCageVariant variant) {
        return BLOCKS.register(variant.id(), () -> new BirdCageBlock(variant, BlockBehaviour.Properties.of().setId(GuaniaoRegistryIds.block(variant.id()))
                .strength(1.5f)
                .sound(SoundType.WOOD)
                .noOcclusion()));
    }

    private static FabricRegistryQueue.Ref<Block> registerBirdBath(BirdBathVariant variant) {
        return BLOCKS.register(variant.id(), () -> new BirdBathBlock(variant, BlockBehaviour.Properties.of().setId(GuaniaoRegistryIds.block(variant.id()))
                .strength(1.8F)
                .sound(variant.soundType())
                .randomTicks()
                .noOcclusion()));
    }
}
