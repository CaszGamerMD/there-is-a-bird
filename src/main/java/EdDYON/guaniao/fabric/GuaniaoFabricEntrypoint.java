package EdDYON.guaniao.fabric;

import EdDYON.guaniao.registry.GuaniaoParticleTypes;
import EdDYON.guaniao.registry.GuaniaoSoundEvents;
import net.fabricmc.api.ModInitializer;

/**
 * Incremental Fabric bootstrap. This class is intentionally not a complete
 * implementation until blocks, items, bird entities, UI, network and all
 * prior initialization paths are migrated. Do not distribute a build yet.
 */
public final class GuaniaoFabricEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        // Preserve every original sound and particle ID.
        GuaniaoSoundEvents.registerAll();
        GuaniaoParticleTypes.registerAll();
        // Remaining original GuaniaoMod registration calls require Fabric ports.
    }
}
