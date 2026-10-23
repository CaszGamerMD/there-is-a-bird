package EdDYON.guaniao.fabric;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.item.Item;
import EdDYON.guaniao.content.bird.FullColorSpawnEggItem;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

/**
 * COMPILE-ONLY API probe for 26.2 registry migration. Never run, register, or
 * package this source: it uses intentionally unregistered probe identifiers.
 */
public final class GuaniaoRegistrationProof {
    private GuaniaoRegistrationProof() { }

    public static Block testBlock() {
        return new Block(BlockBehaviour.Properties.of().setId(
                GuaniaoRegistryIds.block("__compile_only_block")).noCollision());
    }

    public static Item testItem() {
        return new Item(new Item.Properties().setId(
                GuaniaoRegistryIds.item("__compile_only_item")));
    }

    public static EntityType<Pig> testEntity() {
        return EntityType.Builder.<Pig>of(Pig::new, MobCategory.CREATURE)
                .sized(0.9f, 0.9f)
                .clientTrackingRange(8)
                .build(GuaniaoRegistryIds.entity("__compile_only_pig"));
    }

    public static FullColorSpawnEggItem testPaintedSpawnEgg() {
        return new FullColorSpawnEggItem(
                GuaniaoRegistrationProof::testEntity, 0xffffff, 0x000000,
                new Item.Properties().setId(GuaniaoRegistryIds.item("__compile_only_egg")));
    }

    public static BlockEntityType<ChestBlockEntity> testBlockEntity() {
        return FabricBlockEntityTypeBuilder.create(ChestBlockEntity::new, Blocks.CHEST).build();
    }
}
