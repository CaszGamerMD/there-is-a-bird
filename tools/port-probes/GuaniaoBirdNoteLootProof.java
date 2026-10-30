package EdDYON.guaniao.fabric;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import java.util.List;

/** Real Fabric 26.2 compilation gate for keyed post-generation chest drops. */
public final class GuaniaoBirdNoteLootProof {
    private GuaniaoBirdNoteLootProof() { }

    public static void register() {
        LootTableEvents.MODIFY_DROPS.register((holder, context, drops) ->
                apply(holder, context, drops));
    }

    private static void apply(Holder<LootTable> holder, LootContext context, List<ItemStack> drops) {
        holder.unwrapKey().ifPresent(key -> {
            Identifier tableId = key.identifier();
            if (tableId.getPath().startsWith("chests/")) {
                context.getRandom();
            }
        });
    }
}
