package EdDYON.guaniao.fabric;

import EdDYON.guaniao.content.bird.budgerigar.BudgerigarEntity;
import EdDYON.guaniao.content.bird.columbid.PigeonEntity;
import EdDYON.guaniao.content.bird.columbid.SpottedDoveEntity;
import EdDYON.guaniao.content.bird.crow.CrowEntity;
import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
import EdDYON.guaniao.content.bird.seagull.SeagullEntity;
import EdDYON.guaniao.content.bird.kiwi.KiwiEntity;
import EdDYON.guaniao.content.bird.myna.MynaEntity;
import EdDYON.guaniao.content.bird.woodcock.WoodcockEntity;
import EdDYON.guaniao.content.bird.kestrel.KestrelEntity;
import EdDYON.guaniao.content.bird.cassowary.CassowaryEntity;
import EdDYON.guaniao.content.bird.sparrow.SparrowEntity;
import EdDYON.guaniao.content.bird.longtailedtit.LongTailedTitEntity;
import EdDYON.guaniao.content.bird.cockatiel.CockatielEntity;
import EdDYON.guaniao.content.bird.macaw.MacawEntity;
import EdDYON.guaniao.content.bird.umbrellacockatoo.UmbrellaCockatooEntity;
import EdDYON.guaniao.content.bird.cockatoo.CockatooEntity;
import EdDYON.guaniao.content.bird.hummingbird.HummingbirdEntity;
import EdDYON.guaniao.content.bird.kingfisher.KingfisherEntity;
import EdDYON.guaniao.registry.GuaniaoEntityTypes;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

/**
 * One-to-one translation of the 19 original Forge EntityAttributeCreationEvent
 * entries. Keep species-specific attribute providers and the long-tailed tit's
 * original sparrow profile unchanged during migration.
 */
public final class GuaniaoEntityAttributes {
    private GuaniaoEntityAttributes() { }

    public static void registerAll() {
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.NIGHT_HERON.get(), NightHeronEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.SPARROW.get(), SparrowEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.LONG_TAILED_TIT.get(), SparrowEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.COCKATIEL.get(), CockatielEntity.createCockatielAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.MACAW.get(), MacawEntity.createMacawAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.BUDGERIGAR.get(), BudgerigarEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.SPOTTED_DOVE.get(), SpottedDoveEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.PIGEON.get(), PigeonEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.CROW.get(), CrowEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.SEAGULL.get(), SeagullEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.KIWI.get(), KiwiEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.MYNA.get(), MynaEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.WOODCOCK.get(), WoodcockEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.KESTREL.get(), KestrelEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.CASSOWARY.get(), CassowaryEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.UMBRELLA_COCKATOO.get(), UmbrellaCockatooEntity.createUmbrellaCockatooAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.HUMMINGBIRD.get(), HummingbirdEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.COCKATOO.get(), CockatooEntity.createCockatooAttributes().build());
        FabricDefaultAttributeRegistry.register(GuaniaoEntityTypes.KINGFISHER.get(), KingfisherEntity.createAttributes().build());
    }
}
