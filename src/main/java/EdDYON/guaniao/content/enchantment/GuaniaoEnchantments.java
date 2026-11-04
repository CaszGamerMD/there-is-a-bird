package EdDYON.guaniao.content.enchantment;

import EdDYON.guaniao.fabric.GuaniaoRegistryIds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

/** Minecraft 26.2 loads the three feather-fan enchantments from data/guaniao/enchantment. */
public final class GuaniaoEnchantments {
    public static final ResourceKey<Enchantment> BURIAL_PLUME = key("burial_plume");
    public static final ResourceKey<Enchantment> RIVEN_PLUME = key("riven_plume");
    public static final ResourceKey<Enchantment> HUNTING_RETURN = key("hunting_return");

    private GuaniaoEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, GuaniaoRegistryIds.id(name));
    }

    /** Reads enchantments directly from the item component without requiring a world registry. */
    public static boolean has(ItemStack stack, ResourceKey<Enchantment> key) {
        for (Holder<Enchantment> enchantment : stack.getEnchantments().keySet()) {
            if (enchantment.is(key)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnyPlume(ItemStack stack) {
        return has(stack, BURIAL_PLUME) || has(stack, RIVEN_PLUME) || has(stack, HUNTING_RETURN);
    }

    public static void apply(ItemStack stack, ResourceKey<Enchantment> key, HolderLookup.Provider registries) {
        stack.enchant(registries.getOrThrow(key), 1);
    }

    public static boolean isWindFeatherFan(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(GuaniaoRegistryIds.id("wind_feather_fan"));
    }

    /** Original special books remain ordinary custom items, not vanilla enchanted books. */
    public static @Nullable ResourceKey<Enchantment> forBook(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id.equals(GuaniaoRegistryIds.id("burial_plume_book"))) return BURIAL_PLUME;
        if (id.equals(GuaniaoRegistryIds.id("riven_plume_book"))) return RIVEN_PLUME;
        if (id.equals(GuaniaoRegistryIds.id("hunting_return_book"))) return HUNTING_RETURN;
        return null;
    }
}
