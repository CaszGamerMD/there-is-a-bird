package EdDYON.guaniao.fabric;

import EdDYON.guaniao.registry.GuaniaoItems;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;

/**
 * Keep the exact Forge release placement of earthworms in Ingredients and
 * all 19 original bird eggs in Spawn Eggs, in their original order.
 */
public final class GuaniaoVanillaCreativeTabs {
    private GuaniaoVanillaCreativeTabs() { }

    public static void registerCallbacks() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(GuaniaoItems.EARTHWORM.get()));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(entries -> {
                    entries.accept(GuaniaoItems.NIGHT_HERON_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.SPARROW_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.LONG_TAILED_TIT_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.COCKATIEL_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.MACAW_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.BUDGERIGAR_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.SPOTTED_DOVE_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.PIGEON_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.CROW_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.SEAGULL_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.KIWI_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.MYNA_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.WOODCOCK_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.KESTREL_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.CASSOWARY_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.UMBRELLA_COCKATOO_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.HUMMINGBIRD_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.COCKATOO_SPAWN_EGG.get());
                    entries.accept(GuaniaoItems.KINGFISHER_SPAWN_EGG.get());
                });
    }
}
