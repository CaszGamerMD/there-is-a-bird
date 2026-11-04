package EdDYON.guaniao.content.bird.woodcock;

import EdDYON.guaniao.content.bird.BirdTags;
import EdDYON.guaniao.registry.GuaniaoItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/** Preserve earthworm drops from player-shoveled forage soil, without altering block loot. */
public final class EarthwormLootModifier {
    private EarthwormLootModifier() { }

    public static void registerCallbacks() {
        LootTableEvents.MODIFY_DROPS.register((holder, context, generatedLoot) -> {
            BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
            ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
            if (state != null && state.is(BirdTags.WOODCOCK_FORAGE_GROUND)
                    && tool != null && tool.getItem() instanceof ShovelItem
                    && context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player player
                    && !player.isCreative() && !player.isSpectator()
                    && context.getRandom().nextFloat() < 0.20F) {
                generatedLoot.add(new ItemStack(GuaniaoItems.EARTHWORM.get()));
            }
        });
    }
}
