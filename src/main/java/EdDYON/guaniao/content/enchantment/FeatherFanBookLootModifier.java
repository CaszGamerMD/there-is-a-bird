package EdDYON.guaniao.content.enchantment;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/** Authored 4% chest / 0.25% monster rare feather-fan-book drop rules. */
public final class FeatherFanBookLootModifier {
    private static final float CHEST_CHANCE = 0.04F;
    private static final float MONSTER_CHANCE = 0.0025F;

    private FeatherFanBookLootModifier() { }

    public static void registerCallbacks() {
        LootTableEvents.MODIFY_DROPS.register((holder, context, generatedLoot) -> {
            boolean chestLoot = holder.unwrapKey().map(
                    key -> key.location().getPath().startsWith("chests/")).orElse(false);
            boolean monsterLoot = context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Monster;
            float chance = chestLoot ? CHEST_CHANCE : monsterLoot ? MONSTER_CHANCE : 0.0F;
            if (chance > 0.0F && context.getRandom().nextFloat() < chance) {
                generatedLoot.add(FeatherFanEnchantmentBooks.randomBook(context.getRandom()));
            }
        });
    }
}
