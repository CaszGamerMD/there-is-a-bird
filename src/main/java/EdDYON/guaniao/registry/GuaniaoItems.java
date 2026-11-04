package EdDYON.guaniao.registry;

import EdDYON.guaniao.content.earthworm.EarthwormItem;
import EdDYON.guaniao.content.bird.hummingbird.HummingbirdDefinition;
import EdDYON.guaniao.content.bird.kingfisher.KingfisherDefinition;

import java.util.function.Supplier;
import java.util.EnumMap;
import java.util.List;
import EdDYON.guaniao.config.BirdSpecies;
import EdDYON.guaniao.content.camera.FilmItem;
import EdDYON.guaniao.content.camera.NikonD750Item;
import EdDYON.guaniao.content.camera.PhotographItem;
import EdDYON.guaniao.content.guide.BirdGuideItem;
import EdDYON.guaniao.content.bird.FullColorSpawnEggItem;
import EdDYON.guaniao.content.bird.budgerigar.BudgerigarDefinition;
import EdDYON.guaniao.content.bird.columbid.PigeonDefinition;
import EdDYON.guaniao.content.bird.columbid.SpottedDoveDefinition;
import EdDYON.guaniao.content.bird.crow.CrowDefinition;
import EdDYON.guaniao.content.bird.seagull.SeagullDefinition;
import EdDYON.guaniao.content.bird.kiwi.KiwiDefinition;
import EdDYON.guaniao.content.bird.myna.MynaDefinition;
import EdDYON.guaniao.content.bird.woodcock.WoodcockDefinition;
import EdDYON.guaniao.content.bird.kestrel.KestrelDefinition;
import EdDYON.guaniao.content.bird.cassowary.CassowaryDefinition;
import EdDYON.guaniao.content.bird.sparrow.SparrowDefinition;
import EdDYON.guaniao.content.bird.longtailedtit.LongTailedTitDefinition;
import EdDYON.guaniao.content.bird.cockatiel.CockatielDefinition;
import EdDYON.guaniao.content.bird.macaw.MacawDefinition;
import EdDYON.guaniao.content.bird.umbrellacockatoo.UmbrellaCockatooDefinition;
import EdDYON.guaniao.content.bird.cockatoo.CockatooDefinition;
import EdDYON.guaniao.content.bath.BirdBathItem;
import EdDYON.guaniao.content.bath.BirdBathVariant;
import EdDYON.guaniao.content.cage.BirdCageItem;
import EdDYON.guaniao.content.cage.BirdCageVariant;
import EdDYON.guaniao.content.dropping.BirdDroppingItem;
import EdDYON.guaniao.content.dropping.BirdDroppingVariant;
import EdDYON.guaniao.content.enchantment.BurialPlumeBookItem;
import EdDYON.guaniao.content.enchantment.HuntingReturnBookItem;
import EdDYON.guaniao.content.enchantment.RivenPlumeBookItem;
import EdDYON.guaniao.content.fan.FeatherFanItem;
import EdDYON.guaniao.content.feed.BreadcrumbItem;
import EdDYON.guaniao.content.nest.CrowNestItem;
import EdDYON.guaniao.registry.GuaniaoEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import EdDYON.guaniao.fabric.FabricRegistryQueue;
import EdDYON.guaniao.fabric.GuaniaoRegistryIds;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.block.Block;

public final class GuaniaoItems {
    public static final FabricRegistryQueue<Item> ITEMS = new FabricRegistryQueue<>(BuiltInRegistries.ITEM, "guaniao");
    public static final FabricRegistryQueue.Ref<Item> NIGHT_HERON_SPAWN_EGG = GuaniaoItems.registerSpawnEgg("night_heron_spawn_egg", GuaniaoEntityTypes.NIGHT_HERON, 6121331, 14198125);
    public static final FabricRegistryQueue.Ref<Item> SPARROW_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(SparrowDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.SPARROW, SparrowDefinition.SPAWN_EGG_BASE_COLOR, SparrowDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> LONG_TAILED_TIT_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(LongTailedTitDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.LONG_TAILED_TIT, LongTailedTitDefinition.SPAWN_EGG_BASE_COLOR, LongTailedTitDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> COCKATIEL_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(CockatielDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.COCKATIEL, CockatielDefinition.SPAWN_EGG_BASE_COLOR, CockatielDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> MACAW_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(MacawDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.MACAW, MacawDefinition.SPAWN_EGG_BASE_COLOR, MacawDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> BUDGERIGAR_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(BudgerigarDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.BUDGERIGAR, BudgerigarDefinition.SPAWN_EGG_BASE_COLOR, BudgerigarDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> SPOTTED_DOVE_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(SpottedDoveDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.SPOTTED_DOVE, SpottedDoveDefinition.SPAWN_EGG_BASE_COLOR, SpottedDoveDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> PIGEON_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(PigeonDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.PIGEON, PigeonDefinition.SPAWN_EGG_BASE_COLOR, PigeonDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> CROW_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(CrowDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.CROW, CrowDefinition.SPAWN_EGG_BASE_COLOR, CrowDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> SEAGULL_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(SeagullDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.SEAGULL, SeagullDefinition.SPAWN_EGG_BASE_COLOR, SeagullDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> KIWI_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(KiwiDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.KIWI, KiwiDefinition.SPAWN_EGG_BASE_COLOR, KiwiDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> MYNA_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(MynaDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.MYNA, MynaDefinition.SPAWN_EGG_BASE_COLOR, MynaDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> WOODCOCK_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(WoodcockDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.WOODCOCK, WoodcockDefinition.SPAWN_EGG_BASE_COLOR, WoodcockDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> KESTREL_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(KestrelDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.KESTREL, KestrelDefinition.SPAWN_EGG_BASE_COLOR, KestrelDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> CASSOWARY_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(CassowaryDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.CASSOWARY, CassowaryDefinition.SPAWN_EGG_BASE_COLOR, CassowaryDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> UMBRELLA_COCKATOO_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(UmbrellaCockatooDefinition.SPAWN_EGG_ID, GuaniaoEntityTypes.UMBRELLA_COCKATOO, UmbrellaCockatooDefinition.SPAWN_EGG_BASE_COLOR, UmbrellaCockatooDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> COCKATOO_SPAWN_EGG = ITEMS.register(CockatooDefinition.SPAWN_EGG_ID, () ->
            new SpawnEggItem(new Item.Properties()
                    .setId(GuaniaoRegistryIds.item(CockatooDefinition.SPAWN_EGG_ID))
                    .spawnEgg(GuaniaoEntityTypes.COCKATOO.get())));
    public static final FabricRegistryQueue.Ref<Item> HUMMINGBIRD_SPAWN_EGG = GuaniaoItems.registerSpawnEgg(HummingbirdDefinition.SPAWN_EGG_ID,
            GuaniaoEntityTypes.HUMMINGBIRD, HummingbirdDefinition.SPAWN_EGG_BASE_COLOR, HummingbirdDefinition.SPAWN_EGG_SPOT_COLOR);
    public static final FabricRegistryQueue.Ref<Item> KINGFISHER_SPAWN_EGG = ITEMS.register(KingfisherDefinition.SPAWN_EGG_ID, () ->
            new SpawnEggItem(new Item.Properties()
                    .setId(GuaniaoRegistryIds.item(KingfisherDefinition.SPAWN_EGG_ID))
                    .spawnEgg(GuaniaoEntityTypes.KINGFISHER.get())));
    public static final FabricRegistryQueue.Ref<Item> EARTHWORM = ITEMS.register("earthworm", () -> new EarthwormItem(new Item.Properties().setId(GuaniaoRegistryIds.item("earthworm")).stacksTo(64)));
    public static final FabricRegistryQueue.Ref<Item> BREADCRUMBS = ITEMS.register("breadcrumbs", () -> new BreadcrumbItem(new Item.Properties().setId(GuaniaoRegistryIds.item("breadcrumbs"))));
    public static final FabricRegistryQueue.Ref<Item> RAW_FRIES = ITEMS.register("raw_fries", () ->
            new Item(new Item.Properties().setId(GuaniaoRegistryIds.item("raw_fries")).stacksTo(64)));
    public static final FabricRegistryQueue.Ref<Item> COOKED_FRIES = ITEMS.register("cooked_fries", () ->
            new Item(new Item.Properties().setId(GuaniaoRegistryIds.item("cooked_fries")).stacksTo(64).food(new FoodProperties.Builder()
                    .nutrition(4).saturationModifier(0.35F).build())));
    public static final FabricRegistryQueue.Ref<Item> BAGGED_FRIES = ITEMS.register("bagged_fries", () ->
            new BlockItem(GuaniaoBlocks.BAGGED_FRIES.get(), new Item.Properties().setId(GuaniaoRegistryIds.item("bagged_fries")).stacksTo(16)));
    public static final FabricRegistryQueue.Ref<Item> LAXATIVE = ITEMS.register("laxative", () ->
            new Item(new Item.Properties().setId(GuaniaoRegistryIds.item("laxative")).stacksTo(16)));
    public static final FabricRegistryQueue.Ref<Item> BIRD_GUIDE = ITEMS.register("bird_guide", () -> new BirdGuideItem(new Item.Properties().setId(GuaniaoRegistryIds.item("bird_guide")).stacksTo(1)));
    public static final FabricRegistryQueue.Ref<Item> NIKON_D750 = ITEMS.register("nikon_d750", () -> new NikonD750Item(new Item.Properties().setId(GuaniaoRegistryIds.item("nikon_d750")).stacksTo(1)));
    public static final FabricRegistryQueue.Ref<Item> FILM = ITEMS.register("film", () -> new FilmItem(new Item.Properties().setId(GuaniaoRegistryIds.item("film")).stacksTo(1)));
    public static final FabricRegistryQueue.Ref<Item> PHOTOGRAPH = ITEMS.register("photograph", () -> new PhotographItem(new Item.Properties().setId(GuaniaoRegistryIds.item("photograph")).stacksTo(16)));
    public static final FabricRegistryQueue.Ref<Item> FRAME_WORKBENCH = ITEMS.register("frame_workbench", () ->
            new BlockItem(GuaniaoBlocks.FRAME_WORKBENCH.get(), new Item.Properties().setId(GuaniaoRegistryIds.item("frame_workbench"))));
    public static final FabricRegistryQueue.Ref<Item> MUSIC_DISC_UWU_FUNK = ITEMS.register("music_disc_uwu_funk", () ->
            new Item(new Item.Properties().stacksTo(1)
                    .setId(GuaniaoRegistryIds.item("music_disc_uwu_funk"))
                    .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, GuaniaoRegistryIds.id("uwu_funk")))));
    public static final FabricRegistryQueue.Ref<Item> BIRD_DROPPING_1 = ITEMS.register("bird_dropping_1", () -> new BirdDroppingItem(BirdDroppingVariant.ONE, new Item.Properties().setId(GuaniaoRegistryIds.item("bird_dropping_1")).stacksTo(16)));
    public static final FabricRegistryQueue.Ref<Item> BIRD_DROPPING_2 = ITEMS.register("bird_dropping_2", () -> new BirdDroppingItem(BirdDroppingVariant.TWO, new Item.Properties().setId(GuaniaoRegistryIds.item("bird_dropping_2")).stacksTo(16)));
    public static final FabricRegistryQueue.Ref<Item> BIRD_DROPPING_3 = ITEMS.register("bird_dropping_3", () -> new BirdDroppingItem(BirdDroppingVariant.THREE, new Item.Properties().setId(GuaniaoRegistryIds.item("bird_dropping_3")).stacksTo(16)));
    public static final FabricRegistryQueue.Ref<Item> BIRD_DROPPING_4 = ITEMS.register("bird_dropping_4", () -> new BirdDroppingItem(BirdDroppingVariant.FOUR, new Item.Properties().setId(GuaniaoRegistryIds.item("bird_dropping_4")).stacksTo(16)));
    public static final FabricRegistryQueue.Ref<Item> SMALL_BIRD_CAGE = GuaniaoItems.registerBirdCageItem(BirdCageVariant.SMALL, GuaniaoBlocks.SMALL_BIRD_CAGE);
    public static final FabricRegistryQueue.Ref<Item> MEDIUM_BIRD_CAGE = GuaniaoItems.registerBirdCageItem(BirdCageVariant.MEDIUM, GuaniaoBlocks.MEDIUM_BIRD_CAGE);
    public static final FabricRegistryQueue.Ref<Item> LARGE_BIRD_CAGE = GuaniaoItems.registerBirdCageItem(BirdCageVariant.LARGE, GuaniaoBlocks.LARGE_BIRD_CAGE);
    public static final FabricRegistryQueue.Ref<Item> WOODEN_BIRD_BATH = GuaniaoItems.registerBirdBathItem(BirdBathVariant.WOODEN_BIRD_BATH, GuaniaoBlocks.WOODEN_BIRD_BATH);
    public static final FabricRegistryQueue.Ref<Item> STONE_BIRD_BATH = GuaniaoItems.registerBirdBathItem(BirdBathVariant.STONE_BIRD_BATH, GuaniaoBlocks.STONE_BIRD_BATH);
    public static final FabricRegistryQueue.Ref<Item> BIRD_BATH = GuaniaoItems.registerBirdBathItem(BirdBathVariant.BIRD_BATH, GuaniaoBlocks.BIRD_BATH);
    public static final FabricRegistryQueue.Ref<Item> WOODEN_BIRD_BATH_2 = GuaniaoItems.registerBirdBathItem(BirdBathVariant.WOODEN_BIRD_BATH_2, GuaniaoBlocks.WOODEN_BIRD_BATH_2);
    public static final FabricRegistryQueue.Ref<Item> STONE_BIRD_BATH_2 = GuaniaoItems.registerBirdBathItem(BirdBathVariant.STONE_BIRD_BATH_2, GuaniaoBlocks.STONE_BIRD_BATH_2);
    public static final FabricRegistryQueue.Ref<Item> BIRD_BATH_2 = GuaniaoItems.registerBirdBathItem(BirdBathVariant.BIRD_BATH_2, GuaniaoBlocks.BIRD_BATH_2);
    public static final FabricRegistryQueue.Ref<Item> CROW_NEST = ITEMS.register("crow_nest", () ->
            new CrowNestItem(GuaniaoBlocks.CROW_NEST.get(), new Item.Properties().setId(GuaniaoRegistryIds.item("crow_nest"))));
    public static final FabricRegistryQueue.Ref<Item> WIND_FEATHER_FAN = ITEMS.register("wind_feather_fan", () ->
            new FeatherFanItem(new Item.Properties().setId(GuaniaoRegistryIds.item("wind_feather_fan")).durability(250)));
    public static final FabricRegistryQueue.Ref<Item> BURIAL_PLUME_BOOK = ITEMS.register("burial_plume_book", () ->
            new BurialPlumeBookItem(new Item.Properties().setId(GuaniaoRegistryIds.item("burial_plume_book")).stacksTo(1)));
    public static final FabricRegistryQueue.Ref<Item> RIVEN_PLUME_BOOK = ITEMS.register("riven_plume_book", () ->
            new RivenPlumeBookItem(new Item.Properties().setId(GuaniaoRegistryIds.item("riven_plume_book")).stacksTo(1)));
    public static final FabricRegistryQueue.Ref<Item> HUNTING_RETURN_BOOK = ITEMS.register("hunting_return_book", () ->
            new HuntingReturnBookItem(new Item.Properties().setId(GuaniaoRegistryIds.item("hunting_return_book")).stacksTo(1)));
    public static final FabricRegistryQueue.Ref<Item> FEATHER_WHITE = GuaniaoItems.registerFeather("feather_white");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_GREY = GuaniaoItems.registerFeather("feather_grey");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_BLACK = GuaniaoItems.registerFeather("feather_black");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_BROWN = GuaniaoItems.registerFeather("feather_brown");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_CHESTNUT = GuaniaoItems.registerFeather("feather_chestnut");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_RED = GuaniaoItems.registerFeather("feather_red");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_ORANGE = GuaniaoItems.registerFeather("feather_orange");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_YELLOW = GuaniaoItems.registerFeather("feather_yellow");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_GREEN = GuaniaoItems.registerFeather("feather_green");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_BLUE = GuaniaoItems.registerFeather("feather_blue");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_PINK = GuaniaoItems.registerFeather("feather_pink");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_PURPLE = GuaniaoItems.registerFeather("feather_purple");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_RAINBOW = GuaniaoItems.registerFeather("feather_rainbow");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_SPECKLED = GuaniaoItems.registerFeather("feather_speckled");
    public static final FabricRegistryQueue.Ref<Item> FEATHER_BARRED = GuaniaoItems.registerFeather("feather_barred");

    /** Each species' natural feather colors, in display order. The rainbow feather is not here — it only drops from rainbow mutation birds. */
    private static final EnumMap<BirdSpecies, List<FabricRegistryQueue.Ref<Item>>> FEATHERS_BY_SPECIES = new EnumMap<>(BirdSpecies.class);

    static {
        FEATHERS_BY_SPECIES.put(BirdSpecies.NIGHT_HERON, List.of(FEATHER_WHITE, FEATHER_GREY, FEATHER_BLACK));
        FEATHERS_BY_SPECIES.put(BirdSpecies.SPARROW, List.of(FEATHER_CHESTNUT, FEATHER_BROWN, FEATHER_WHITE, FEATHER_BLACK));
        FEATHERS_BY_SPECIES.put(BirdSpecies.LONG_TAILED_TIT, List.of(FEATHER_WHITE, FEATHER_PINK, FEATHER_BLACK, FEATHER_GREY));
        FEATHERS_BY_SPECIES.put(BirdSpecies.COCKATIEL, List.of(FEATHER_GREY, FEATHER_YELLOW, FEATHER_WHITE, FEATHER_ORANGE));
        FEATHERS_BY_SPECIES.put(BirdSpecies.MACAW, List.of(FEATHER_RED, FEATHER_BLUE, FEATHER_YELLOW, FEATHER_GREEN, FEATHER_WHITE));
        FEATHERS_BY_SPECIES.put(BirdSpecies.BUDGERIGAR, List.of(FEATHER_GREEN, FEATHER_BLUE, FEATHER_YELLOW, FEATHER_WHITE, FEATHER_GREY));
        FEATHERS_BY_SPECIES.put(BirdSpecies.SPOTTED_DOVE, List.of(FEATHER_BROWN, FEATHER_PINK, FEATHER_WHITE, FEATHER_BLACK, FEATHER_SPECKLED));
        FEATHERS_BY_SPECIES.put(BirdSpecies.PIGEON, List.of(FEATHER_GREY, FEATHER_WHITE, FEATHER_BLACK, FEATHER_BROWN, FEATHER_BARRED));
        FEATHERS_BY_SPECIES.put(BirdSpecies.CROW, List.of(FEATHER_BLACK, FEATHER_GREY, FEATHER_PURPLE));
        FEATHERS_BY_SPECIES.put(BirdSpecies.SEAGULL, List.of(FEATHER_WHITE, FEATHER_GREY, FEATHER_BLACK));
        FEATHERS_BY_SPECIES.put(BirdSpecies.KIWI, List.of(FEATHER_BROWN, FEATHER_GREY, FEATHER_BLACK, FEATHER_CHESTNUT));
        FEATHERS_BY_SPECIES.put(BirdSpecies.MYNA, List.of(FEATHER_BLACK, FEATHER_BROWN, FEATHER_GREY, FEATHER_WHITE));
        FEATHERS_BY_SPECIES.put(BirdSpecies.WOODCOCK, List.of(FEATHER_BROWN, FEATHER_CHESTNUT, FEATHER_SPECKLED, FEATHER_BLACK));
        FEATHERS_BY_SPECIES.put(BirdSpecies.KESTREL, List.of(FEATHER_CHESTNUT, FEATHER_BROWN, FEATHER_BLACK, FEATHER_SPECKLED));
        FEATHERS_BY_SPECIES.put(BirdSpecies.CASSOWARY, List.of(FEATHER_BLACK, FEATHER_BLUE, FEATHER_BROWN));
        FEATHERS_BY_SPECIES.put(BirdSpecies.UMBRELLA_COCKATOO, List.of(FEATHER_WHITE, FEATHER_YELLOW, FEATHER_GREY, FEATHER_ORANGE));
        FEATHERS_BY_SPECIES.put(BirdSpecies.HUMMINGBIRD, List.of(FEATHER_GREEN, FEATHER_WHITE));
        FEATHERS_BY_SPECIES.put(BirdSpecies.COCKATOO, List.of(FEATHER_WHITE, FEATHER_YELLOW, FEATHER_GREY));
        FEATHERS_BY_SPECIES.put(BirdSpecies.KINGFISHER, List.of(FEATHER_BLUE, FEATHER_GREEN, FEATHER_ORANGE));
    }

    private GuaniaoItems() {
    }

    private static FabricRegistryQueue.Ref<Item> registerSpawnEgg(String id, Supplier<? extends EntityType<? extends Mob>> entityTypeSupplier, int baseColor, int spotColor) {
        return ITEMS.register(id, () -> new FullColorSpawnEggItem(entityTypeSupplier, baseColor, spotColor,
                new Item.Properties().setId(GuaniaoRegistryIds.item(id))));
    }

    private static FabricRegistryQueue.Ref<Item> registerFeather(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties().setId(GuaniaoRegistryIds.item(id)).stacksTo(64)));
    }

    /** A species' natural feather colors, or an empty list for non-birds. */
    public static List<Item> feathersFor(BirdSpecies species) {
        List<FabricRegistryQueue.Ref<Item>> feathers = FEATHERS_BY_SPECIES.get(species);
        if (feathers == null || feathers.isEmpty()) {
            return List.of();
        }
        return feathers.stream().map(FabricRegistryQueue.Ref::get).toList();
    }

    private static FabricRegistryQueue.Ref<Item> registerBirdCageItem(BirdCageVariant variant, Supplier<? extends Block> blockSupplier) {
        return ITEMS.register(variant.id(), () -> new BirdCageItem(variant, blockSupplier.get(),
                new Item.Properties().setId(GuaniaoRegistryIds.item(variant.id()))));
    }

    private static FabricRegistryQueue.Ref<Item> registerBirdBathItem(BirdBathVariant variant, Supplier<? extends Block> blockSupplier) {
        return ITEMS.register(variant.id(), () -> new BirdBathItem(variant, blockSupplier.get(),
                new Item.Properties().setId(GuaniaoRegistryIds.item(variant.id()))));
    }
}
