package EdDYON.guaniao.fabric;

import EdDYON.guaniao.config.BirdConfigManager;
import EdDYON.guaniao.config.HummingbirdConfig;
import EdDYON.guaniao.content.bird.hummingbird.HummingbirdEcologyEvents;
import EdDYON.guaniao.content.dropping.BirdDroppingUtil;
import EdDYON.guaniao.content.dropping.PrankEntityAttachments;
import EdDYON.guaniao.registry.GuaniaoLootModifierSerializers;
import EdDYON.guaniao.content.dropping.BirdDroppingPressurePlatePulse;
import EdDYON.guaniao.event.BirdConfigLifecycleEvents;
import EdDYON.guaniao.event.BirdPopulationTracker;
import EdDYON.guaniao.event.BirdAdvancementEvents;
import EdDYON.guaniao.event.CassowaryThreatEvents;
import EdDYON.guaniao.event.PollutedCakeEvents;
import EdDYON.guaniao.event.BirdColonySpawnRules;
import EdDYON.guaniao.event.BirdFlybySpawnEvents;
import EdDYON.guaniao.event.BirdRepopulationEvents;
import EdDYON.guaniao.event.SparrowTideManager;
import EdDYON.guaniao.event.BirdServerTickManager;
import EdDYON.guaniao.event.BirdRuntimeConfigEvents;
import EdDYON.guaniao.event.BirdHeadPerchEvents;
import EdDYON.guaniao.event.BirdShearEvents;
import EdDYON.guaniao.event.FeatherFanCombatEvents;
import EdDYON.guaniao.event.PrankFoodEvents;
import EdDYON.guaniao.event.BirdNoteEvents;
import EdDYON.guaniao.command.BirdConfigCommands;
import EdDYON.guaniao.command.PhotoAdminCommands;
import EdDYON.guaniao.content.bird.flight.BirdFlightLock;
import EdDYON.guaniao.content.bird.BirdEntityAttachments;
import EdDYON.guaniao.content.bird.BirdScanBudget;
import EdDYON.guaniao.content.bird.BirdDroppingAreaLimiter;
import EdDYON.guaniao.content.bird.flock.BirdFlockManager;
import EdDYON.guaniao.content.bird.brain.BirdHabitatCache;
import EdDYON.guaniao.content.bird.nightheron.NightHeronLandingSelector;
import EdDYON.guaniao.content.bird.nightheron.NightHeronShorelineCache;
import EdDYON.guaniao.content.bird.BirdFlockSoundLimiter;
import EdDYON.guaniao.event.CrowNestLifecycleEvents;
import EdDYON.guaniao.event.EarthwormRainEvents;
import EdDYON.guaniao.event.PhotoTransferEvents;
import EdDYON.guaniao.event.PhotoLegacyMigrationEvents;
import EdDYON.guaniao.registry.GuaniaoBlockEntityTypes;
import EdDYON.guaniao.registry.GuaniaoBlocks;
import EdDYON.guaniao.registry.GuaniaoCreativeTabs;
import EdDYON.guaniao.registry.GuaniaoEntityTypes;
import EdDYON.guaniao.registry.GuaniaoFeatures;
import EdDYON.guaniao.registry.GuaniaoItems;
import EdDYON.guaniao.registry.GuaniaoMenuTypes;
import EdDYON.guaniao.registry.GuaniaoParticleTypes;
import EdDYON.guaniao.registry.GuaniaoRecipeSerializers;
import EdDYON.guaniao.registry.GuaniaoSoundEvents;
import EdDYON.guaniao.world.GuaniaoBirdSpawnRegistry;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric 26.2 bootstrap. The ordering mirrors the source Forge registry
 * dependencies: blocks before block entities, bird types before eggs, and
 * every item before the creative tab.
 *
 * This is not a runnable migration yet. Other source registries and systems
 * (menus, recipes, loot, enchantments, packets, spawning, animations, camera,
 * and other Forge events) must also be ported before release.
 */
public final class GuaniaoFabricEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        GuaniaoFabricNetworking.register();
        BirdFlightLock.registerAttachments();
        BirdEntityAttachments.registerAttachments();
        PrankEntityAttachments.registerAttachments();
        GuaniaoSoundEvents.registerAll();
        GuaniaoParticleTypes.registerAll();

        GuaniaoBlocks.BLOCKS.registerAll();
        GuaniaoBlockEntityTypes.BLOCK_ENTITY_TYPES.registerAll();
        GuaniaoEntityTypes.ENTITY_TYPES.registerAll();
        GuaniaoEntityAttributes.registerAll();
        GuaniaoBirdSpawnRegistry.registerCallbacks();
        GuaniaoItems.ITEMS.registerAll();
        GuaniaoMenuTypes.MENU_TYPES.registerAll();
        BirdDroppingUtil.registerCompostables();
        GuaniaoRecipeSerializers.RECIPE_SERIALIZERS.registerAll();
        GuaniaoFeatures.FEATURES.registerAll();
        GuaniaoCreativeTabs.CREATIVE_MODE_TABS.registerAll();
        GuaniaoVanillaCreativeTabs.registerCallbacks();

        // Preserve the source global/world-scoped configuration lifecycle.
        BirdConfigManager.loadOrCreateDefaults();
        HummingbirdConfig.loadOrCreateDefaults();
        BirdConfigLifecycleEvents.registerCallbacks();

        // Existing behavior: track active birds, tick rain worms and clean up
        // transient nest and photo state at exactly the corresponding hooks.
        BirdServerTickManager.registerCallbacks();
        BirdScanBudget.registerCallbacks();
        BirdDroppingAreaLimiter.registerCallbacks();
        BirdFlockManager.registerCallbacks();
        BirdHabitatCache.registerCallbacks();
        NightHeronLandingSelector.registerCallbacks();
        NightHeronShorelineCache.registerCallbacks();
        BirdFlockSoundLimiter.registerCallbacks();
        HummingbirdEcologyEvents.registerCallbacks();
        BirdPopulationTracker.registerCallbacks();
        BirdAdvancementEvents.registerCallbacks();
        CassowaryThreatEvents.registerCallbacks();
        PollutedCakeEvents.registerCallbacks();
        BirdDroppingPressurePlatePulse.registerCallbacks();
        BirdColonySpawnRules.registerCallbacks();
        BirdFlybySpawnEvents.registerCallbacks();
        BirdRepopulationEvents.registerCallbacks();
        SparrowTideManager.registerCallbacks();
        EarthwormRainEvents.registerCallbacks();
        CrowNestLifecycleEvents.registerCallbacks();
        PhotoLegacyMigrationEvents.registerCallbacks();
        PhotoTransferEvents.registerCallbacks();
        BirdRuntimeConfigEvents.registerCallbacks();
        BirdHeadPerchEvents.registerCallbacks();
        BirdShearEvents.registerCallbacks();
        FeatherFanCombatEvents.registerCallbacks();
        PrankFoodEvents.registerCallbacks();
        BirdNoteEvents.registerCallbacks();
        BirdConfigCommands.registerCallbacks();
        PhotoAdminCommands.registerCallbacks();
        GuaniaoLootModifierSerializers.registerCallbacks();

        // TODO: complete remaining menu, enchantment, model/GeckoLib 5,
        // client-renderer and gameplay event migrations. All 15 original
        // networking payload types are registered, but full build and
        // client/server runtime verification are still outstanding.
    }
}
