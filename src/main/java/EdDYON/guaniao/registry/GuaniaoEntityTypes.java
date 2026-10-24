package EdDYON.guaniao.registry;

import EdDYON.guaniao.content.bird.BirdVisibility;
import EdDYON.guaniao.content.bird.hummingbird.HummingbirdDefinition;
import EdDYON.guaniao.content.bird.hummingbird.HummingbirdEntity;
import EdDYON.guaniao.content.bird.kingfisher.KingfisherDefinition;
import EdDYON.guaniao.content.bird.kingfisher.KingfisherEntity;

import EdDYON.guaniao.content.earthworm.EarthwormEntity;

import EdDYON.guaniao.content.bird.budgerigar.BudgerigarDefinition;
import EdDYON.guaniao.content.bird.budgerigar.BudgerigarEntity;
import EdDYON.guaniao.content.bird.columbid.PigeonDefinition;
import EdDYON.guaniao.content.bird.columbid.PigeonEntity;
import EdDYON.guaniao.content.bird.columbid.SpottedDoveDefinition;
import EdDYON.guaniao.content.bird.columbid.SpottedDoveEntity;
import EdDYON.guaniao.content.bird.crow.CrowDefinition;
import EdDYON.guaniao.content.bird.crow.CrowEntity;
import EdDYON.guaniao.content.bird.nightheron.NightHeronDefinition;
import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
import EdDYON.guaniao.content.bird.seagull.SeagullDefinition;
import EdDYON.guaniao.content.bird.seagull.SeagullEntity;
import EdDYON.guaniao.content.bird.kiwi.KiwiDefinition;
import EdDYON.guaniao.content.bird.kiwi.KiwiEntity;
import EdDYON.guaniao.content.bird.myna.MynaDefinition;
import EdDYON.guaniao.content.bird.myna.MynaEntity;
import EdDYON.guaniao.content.bird.woodcock.WoodcockDefinition;
import EdDYON.guaniao.content.bird.woodcock.WoodcockEntity;
import EdDYON.guaniao.content.bird.kestrel.KestrelDefinition;
import EdDYON.guaniao.content.bird.kestrel.KestrelEntity;
import EdDYON.guaniao.content.bird.cassowary.CassowaryDefinition;
import EdDYON.guaniao.content.bird.cassowary.CassowaryEntity;
import EdDYON.guaniao.content.bird.sparrow.SparrowDefinition;
import EdDYON.guaniao.content.bird.sparrow.SparrowEntity;
import EdDYON.guaniao.content.bird.longtailedtit.LongTailedTitDefinition;
import EdDYON.guaniao.content.bird.longtailedtit.LongTailedTitEntity;
import EdDYON.guaniao.content.bird.cockatiel.CockatielDefinition;
import EdDYON.guaniao.content.bird.cockatiel.CockatielEntity;
import EdDYON.guaniao.content.bird.macaw.MacawDefinition;
import EdDYON.guaniao.content.bird.macaw.MacawEntity;
import EdDYON.guaniao.content.bird.umbrellacockatoo.UmbrellaCockatooDefinition;
import EdDYON.guaniao.content.bird.umbrellacockatoo.UmbrellaCockatooEntity;
import EdDYON.guaniao.content.bird.cockatoo.CockatooDefinition;
import EdDYON.guaniao.content.bird.cockatoo.CockatooEntity;
import EdDYON.guaniao.content.camera.PhotographEntity;
import EdDYON.guaniao.content.dropping.BirdDroppingProjectileEntity;
import EdDYON.guaniao.content.dropping.BirdDroppingSplatEntity;
import EdDYON.guaniao.content.fan.FeatherFanProjectileEntity;
import EdDYON.guaniao.fabric.FabricRegistryQueue;
import EdDYON.guaniao.fabric.GuaniaoRegistryIds;
import EdDYON.guaniao.fabric.GuaniaoNaturalSpawnRules;
import EdDYON.guaniao.event.BirdSpawnPlacements;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;

public final class GuaniaoEntityTypes {
    public static final MobCategory BIRD = MobCategory.CREATURE;
    public static final FabricRegistryQueue<EntityType<?>> ENTITY_TYPES = new FabricRegistryQueue<>(BuiltInRegistries.ENTITY_TYPE, "guaniao");
    public static final FabricRegistryQueue.Ref<EntityType<NightHeronEntity>> NIGHT_HERON = GuaniaoEntityTypes.registerCreature(NightHeronDefinition.ENTITY_ID, NightHeronEntity::new, NightHeronDefinition.WIDTH, NightHeronDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NightHeronEntity::canSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<SparrowEntity>> SPARROW = GuaniaoEntityTypes.registerCreature(SparrowDefinition.ENTITY_ID, SparrowEntity::new, SparrowDefinition.WIDTH, SparrowDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SparrowEntity::canSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<LongTailedTitEntity>> LONG_TAILED_TIT = GuaniaoEntityTypes.registerCreature(LongTailedTitDefinition.ENTITY_ID, LongTailedTitEntity::new, LongTailedTitDefinition.WIDTH, LongTailedTitDefinition.HEIGHT, BirdSpawnPlacements.ON_GROUND_OR_LEAVES, Heightmap.Types.MOTION_BLOCKING, LongTailedTitEntity::canLongTailedTitSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<CockatielEntity>> COCKATIEL = GuaniaoEntityTypes.registerCreature(CockatielDefinition.ENTITY_ID, CockatielEntity::new, CockatielDefinition.WIDTH, CockatielDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CockatielEntity::canCockatielSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<MacawEntity>> MACAW = GuaniaoEntityTypes.registerCreature(MacawDefinition.ENTITY_ID, MacawEntity::new, MacawDefinition.WIDTH, MacawDefinition.HEIGHT, BirdSpawnPlacements.ON_GROUND_OR_LEAVES, Heightmap.Types.MOTION_BLOCKING, MacawEntity::canMacawSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<BudgerigarEntity>> BUDGERIGAR = GuaniaoEntityTypes.registerCreature(BudgerigarDefinition.ENTITY_ID, BudgerigarEntity::new, BudgerigarDefinition.WIDTH, BudgerigarDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BudgerigarEntity::canSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<SpottedDoveEntity>> SPOTTED_DOVE = GuaniaoEntityTypes.registerCreature(SpottedDoveDefinition.ENTITY_ID, SpottedDoveEntity::new, SpottedDoveDefinition.WIDTH, SpottedDoveDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpottedDoveEntity::canSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<PigeonEntity>> PIGEON = GuaniaoEntityTypes.registerCreature(PigeonDefinition.ENTITY_ID, PigeonEntity::new, PigeonDefinition.WIDTH, PigeonDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PigeonEntity::canSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<CrowEntity>> CROW = GuaniaoEntityTypes.registerCreature(CrowDefinition.ENTITY_ID, CrowEntity::new, CrowDefinition.WIDTH, CrowDefinition.HEIGHT, BirdSpawnPlacements.ON_GROUND_OR_LEAVES, Heightmap.Types.MOTION_BLOCKING, CrowEntity::canSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<SeagullEntity>> SEAGULL = GuaniaoEntityTypes.registerCreature(SeagullDefinition.ENTITY_ID, SeagullEntity::new, SeagullDefinition.WIDTH, SeagullDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SeagullEntity::canSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<KiwiEntity>> KIWI = GuaniaoEntityTypes.registerCreature(KiwiDefinition.ENTITY_ID, KiwiEntity::new, KiwiDefinition.WIDTH, KiwiDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, KiwiEntity::canSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<MynaEntity>> MYNA = GuaniaoEntityTypes.registerCreature(MynaDefinition.ENTITY_ID, MynaEntity::new, MynaDefinition.WIDTH, MynaDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MynaEntity::canMynaSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<WoodcockEntity>> WOODCOCK = GuaniaoEntityTypes.registerCreature(WoodcockDefinition.ENTITY_ID, WoodcockEntity::new, WoodcockDefinition.WIDTH, WoodcockDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WoodcockEntity::canWoodcockSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<KestrelEntity>> KESTREL = GuaniaoEntityTypes.registerCreature(KestrelDefinition.ENTITY_ID, KestrelEntity::new, KestrelDefinition.WIDTH, KestrelDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, KestrelEntity::canSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<CassowaryEntity>> CASSOWARY = GuaniaoEntityTypes.registerCreature(CassowaryDefinition.ENTITY_ID, CassowaryEntity::new, CassowaryDefinition.WIDTH, CassowaryDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CassowaryEntity::canSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<UmbrellaCockatooEntity>> UMBRELLA_COCKATOO = GuaniaoEntityTypes.registerCreature(UmbrellaCockatooDefinition.ENTITY_ID, UmbrellaCockatooEntity::new, UmbrellaCockatooDefinition.WIDTH, UmbrellaCockatooDefinition.HEIGHT, BirdSpawnPlacements.ON_GROUND_OR_LEAVES, Heightmap.Types.MOTION_BLOCKING, UmbrellaCockatooEntity::canUmbrellaCockatooSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<CockatooEntity>> COCKATOO = GuaniaoEntityTypes.registerCreature(CockatooDefinition.ENTITY_ID, CockatooEntity::new, CockatooDefinition.WIDTH, CockatooDefinition.HEIGHT, BirdSpawnPlacements.ON_GROUND_OR_LEAVES, Heightmap.Types.MOTION_BLOCKING, CockatooEntity::canCockatooSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<HummingbirdEntity>> HUMMINGBIRD = GuaniaoEntityTypes.registerCreature(HummingbirdDefinition.ENTITY_ID, HummingbirdEntity::new, HummingbirdDefinition.WIDTH, HummingbirdDefinition.HEIGHT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, HummingbirdEntity::canSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<KingfisherEntity>> KINGFISHER = GuaniaoEntityTypes.registerCreature(KingfisherDefinition.ENTITY_ID, KingfisherEntity::new, KingfisherDefinition.WIDTH, KingfisherDefinition.HEIGHT, BirdSpawnPlacements.ON_GROUND_OR_LEAVES, Heightmap.Types.MOTION_BLOCKING, KingfisherEntity::canSpawn);
    public static final FabricRegistryQueue.Ref<EntityType<PhotographEntity>> PHOTOGRAPH = ENTITY_TYPES.register("photograph", () ->
            EntityType.Builder.<PhotographEntity>of(PhotographEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(GuaniaoRegistryIds.entity("photograph")));
    public static final FabricRegistryQueue.Ref<EntityType<BirdDroppingProjectileEntity>> BIRD_DROPPING_PROJECTILE = ENTITY_TYPES.register("bird_dropping_projectile", () ->
            EntityType.Builder.<BirdDroppingProjectileEntity>of(BirdDroppingProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(GuaniaoRegistryIds.entity("bird_dropping_projectile")));
    public static final FabricRegistryQueue.Ref<EntityType<BirdDroppingSplatEntity>> BIRD_DROPPING_SPLAT = ENTITY_TYPES.register("bird_dropping_splat", () ->
            EntityType.Builder.<BirdDroppingSplatEntity>of(BirdDroppingSplatEntity::new, MobCategory.MISC)
                    .sized(0.45F, 0.12F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build(GuaniaoRegistryIds.entity("bird_dropping_splat")));
    public static final FabricRegistryQueue.Ref<EntityType<FeatherFanProjectileEntity>> FEATHER_FAN_PROJECTILE = ENTITY_TYPES.register("feather_fan_projectile", () ->
            EntityType.Builder.<FeatherFanProjectileEntity>of(FeatherFanProjectileEntity::new, MobCategory.MISC)
                    .sized(0.65F, 0.15F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(GuaniaoRegistryIds.entity("feather_fan_projectile")));

    public static final FabricRegistryQueue.Ref<EntityType<EarthwormEntity>> EARTHWORM = ENTITY_TYPES.register("earthworm", () ->
            EntityType.Builder.<EarthwormEntity>of(EarthwormEntity::new, MobCategory.MISC)
                    .sized(0.32F, 0.05F).clientTrackingRange(6).updateInterval(3)
                    .build(GuaniaoRegistryIds.entity("earthworm")));

    private GuaniaoEntityTypes() {
    }

    /**
     * Register original species placement rules at type-construction time. Fabric
     * installs the custom heightmap and spawn predicate through its mob builder.
     */
    private static <T extends Mob> FabricRegistryQueue.Ref<EntityType<T>> registerCreature(
            String id, EntityType.EntityFactory<T> factory, float width, float height,
            SpawnPlacementType placementType, Heightmap.Types heightmap,
            SpawnPlacements.SpawnPredicate<T> originalPredicate) {
        return ENTITY_TYPES.register(id, () ->
                FabricEntityType.Builder.createMob(factory, BIRD,
                        builder -> builder.spawnPlacement(placementType, heightmap,
                                (type, level, reason, pos, random) ->
                                        reason != EntitySpawnReason.CHUNK_GENERATION
                                                && GuaniaoNaturalSpawnRules.allowsPlacement(type, level, reason, pos)
                                                && originalPredicate.test(type, level, reason, pos, random)))
                        .sized(width, height)
                        .clientTrackingRange(BirdVisibility.TRACKING_RANGE_CHUNKS)
                        .build(GuaniaoRegistryIds.entity(id)));
    }
}
