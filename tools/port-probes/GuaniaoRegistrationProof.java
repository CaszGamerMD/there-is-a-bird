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
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.Mob;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
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

    public static Item testFriesNutrition() {
        return new Item(new Item.Properties()
                .setId(GuaniaoRegistryIds.item("__compile_only_fries"))
                .food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.35F).build()));
    }

    public static Item testMusicDisc() {
        return new Item(new Item.Properties()
                .setId(GuaniaoRegistryIds.item("__compile_only_disc"))
                .stacksTo(1)
                .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, GuaniaoRegistryIds.id("uwu_funk"))));
    }

    public static FullColorSpawnEggItem testPaintedSpawnEgg() {
        return new FullColorSpawnEggItem(
                GuaniaoRegistrationProof::testEntity, 0xffffff, 0x000000,
                new Item.Properties().setId(GuaniaoRegistryIds.item("__compile_only_egg")));
    }

    public static void proofVanillaCreativeTabCallbacks() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(entries -> entries.accept(Items.EGG));
    }

    public static void proofBirdAttributes() {
        FabricDefaultAttributeRegistry.register(testEntity(), Mob.createMobAttributes());
    }

    public static BlockEntityType<ChestBlockEntity> testBlockEntity() {
        return FabricBlockEntityTypeBuilder.create(ChestBlockEntity::new, Blocks.CHEST).build();
    }
}
