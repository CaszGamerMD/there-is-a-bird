package EdDYON.guaniao.fabric;

import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Verifies the 26.2 identifier parser used by Kestrel learning and Crow models. */
public final class GuaniaoBirdIdentifierProof {
    private GuaniaoBirdIdentifierProof() { }

    public static Identifier learnedItem(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public static boolean parseSavedIdentifier(String saved) {
        Identifier parsed = Identifier.tryParse(saved);
        return parsed != null && BuiltInRegistries.ITEM.containsKey(parsed);
    }
}
