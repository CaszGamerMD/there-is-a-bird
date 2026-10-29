package EdDYON.guaniao.content.note;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric replacement for the original global chest-loot modifier.
 * MODIFY_DROPS sees the real loot context and keeps all existing chest items.
 */
public final class BirdNoteLootModifier {
    private BirdNoteLootModifier() { }

    public static void registerCallbacks() {
        LootTableEvents.MODIFY_DROPS.register((holder, context, generatedLoot) -> {
            holder.unwrapKey().ifPresent(key -> {
                ItemStack note = BirdNoteContent.roll(context, key.location());
                if (!note.isEmpty()) {
                    generatedLoot.add(note);
                }
            });
        });
    }
}
