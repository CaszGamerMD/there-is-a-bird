package EdDYON.guaniao.registry;

import EdDYON.guaniao.content.enchantment.FeatherFanBookLootModifier;
import EdDYON.guaniao.content.note.BirdNoteLootModifier;
import EdDYON.guaniao.content.bird.woodcock.EarthwormLootModifier;

/**
 * Fabric runtime loot listeners replace Forge's global-loot-modifier registry.
 * All three original authorship/drop formulas remain separate and additive.
 */
public final class GuaniaoLootModifierSerializers {
    private GuaniaoLootModifierSerializers() { }

    public static void registerCallbacks() {
        BirdNoteLootModifier.registerCallbacks();
        FeatherFanBookLootModifier.registerCallbacks();
        EarthwormLootModifier.registerCallbacks();
    }
}
