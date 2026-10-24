package EdDYON.guaniao.fabric;

import EdDYON.guaniao.config.BirdConfigManager;
import EdDYON.guaniao.event.BirdConfigLifecycleEvents;
import EdDYON.guaniao.event.BirdPopulationTracker;
import EdDYON.guaniao.event.BirdAdvancementEvents;
import EdDYON.guaniao.event.CassowaryThreatEvents;
import EdDYON.guaniao.event.PollutedCakeEvents;
import EdDYON.guaniao.event.BirdColonySpawnRules;
import EdDYON.guaniao.event.BirdServerTickManager;
import EdDYON.guaniao.event.BirdRuntimeConfigEvents;
import EdDYON.guaniao.event.BirdHeadPerchEvents;
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
import EdDYON.guaniao.registry.GuaniaoParticleTypes;
import EdDYON.guaniao.registry.GuaniaoRecipeSerializers;
import EdDYON.guaniao.registry.GuaniaoSoundEvents;
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
        GuaniaoSoundEvents.registerAll();
        GuaniaoParticleTypes.registerAll();

        GuaniaoBlocks.BLOCKS.registerAll();
        GuaniaoBlockEntityTypes.BLOCK_ENTITY_TYPES.registerAll();
        GuaniaoEntityTypes.ENTITY_TYPES.registerAll();
        GuaniaoEntityAttributes.registerAll();
        GuaniaoItems.ITEMS.registerAll();
        GuaniaoRecipeSerializers.RECIPE_SERIALIZERS.registerAll();
        GuaniaoFeatures.FEATURES.registerAll();
        GuaniaoCreativeTabs.CREATIVE_MODE_TABS.registerAll();
        GuaniaoVanillaCreativeTabs.registerCallbacks();

        // Preserve the source global/world-scoped configuration lifecycle.
        BirdConfigManager.loadOrCreateDefaults();
        BirdConfigLifecycleEvents.registerCallbacks();

        // Existing behavior: track active birds, tick rain worms and clean up
        // transient nest and photo state at exactly the corresponding hooks.
        BirdServerTickManager.registerCallbacks();
        BirdPopulationTracker.registerCallbacks();
        BirdAdvancementEvents.registerCallbacks();
        CassowaryThreatEvents.registerCallbacks();
        PollutedCakeEvents.registerCallbacks();
        BirdColonySpawnRules.registerCallbacks();
        EarthwormRainEvents.registerCallbacks();
        CrowNestLifecycleEvents.registerCallbacks();
        PhotoLegacyMigrationEvents.registerCallbacks();
        PhotoTransferEvents.registerCallbacks();
        BirdRuntimeConfigEvents.registerCallbacks();
        BirdHeadPerchEvents.registerCallbacks();

        // TODO: register migrated menus, recipes, effects, enchantments,
        // networking, spawn placements, client renderers and all other
        // original event handlers before this becomes a runnable release.
    }
}
