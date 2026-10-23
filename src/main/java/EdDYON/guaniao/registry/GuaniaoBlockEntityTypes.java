package EdDYON.guaniao.registry;

import EdDYON.guaniao.content.bath.BirdBathBlockEntity;
import EdDYON.guaniao.content.cage.BirdCageBlockEntity;
import EdDYON.guaniao.content.nest.CrowNestBlockEntity;
import EdDYON.guaniao.content.food.BaggedFriesBlockEntity;
import EdDYON.guaniao.content.camera.frame.FrameWorkbenchBlockEntity;
import EdDYON.guaniao.fabric.FabricRegistryQueue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class GuaniaoBlockEntityTypes {
    public static final FabricRegistryQueue<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new FabricRegistryQueue<>(BuiltInRegistries.BLOCK_ENTITY_TYPE, "guaniao");

    public static final FabricRegistryQueue.Ref<BlockEntityType<BirdCageBlockEntity>> BIRD_CAGE = BLOCK_ENTITY_TYPES.register("bird_cage", () ->
            BlockEntityType.Builder.of(BirdCageBlockEntity::new,
                    GuaniaoBlocks.SMALL_BIRD_CAGE.get(),
                    GuaniaoBlocks.MEDIUM_BIRD_CAGE.get(),
                    GuaniaoBlocks.LARGE_BIRD_CAGE.get()).build(null));

    public static final FabricRegistryQueue.Ref<BlockEntityType<BirdBathBlockEntity>> BIRD_BATH = BLOCK_ENTITY_TYPES.register("bird_bath", () ->
            BlockEntityType.Builder.of(BirdBathBlockEntity::new,
                    GuaniaoBlocks.WOODEN_BIRD_BATH.get(),
                    GuaniaoBlocks.STONE_BIRD_BATH.get(),
                    GuaniaoBlocks.BIRD_BATH.get(),
                    GuaniaoBlocks.WOODEN_BIRD_BATH_2.get(),
                    GuaniaoBlocks.STONE_BIRD_BATH_2.get(),
                    GuaniaoBlocks.BIRD_BATH_2.get()).build(null));

    public static final FabricRegistryQueue.Ref<BlockEntityType<CrowNestBlockEntity>> CROW_NEST = BLOCK_ENTITY_TYPES.register("crow_nest", () ->
            BlockEntityType.Builder.of(CrowNestBlockEntity::new, GuaniaoBlocks.CROW_NEST.get()).build(null));

    public static final FabricRegistryQueue.Ref<BlockEntityType<BaggedFriesBlockEntity>> BAGGED_FRIES = BLOCK_ENTITY_TYPES.register("bagged_fries", () ->
            BlockEntityType.Builder.of(BaggedFriesBlockEntity::new, GuaniaoBlocks.BAGGED_FRIES.get()).build(null));

    public static final FabricRegistryQueue.Ref<BlockEntityType<FrameWorkbenchBlockEntity>> FRAME_WORKBENCH = BLOCK_ENTITY_TYPES.register("frame_workbench", () ->
            BlockEntityType.Builder.of(FrameWorkbenchBlockEntity::new, GuaniaoBlocks.FRAME_WORKBENCH.get()).build(null));

    private GuaniaoBlockEntityTypes() {
    }
}
