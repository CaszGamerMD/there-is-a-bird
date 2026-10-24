package EdDYON.guaniao.content.dropping;

import EdDYON.guaniao.registry.GuaniaoRecipeSerializers;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BirdDroppingFoodRecipe extends CustomRecipe {
    public static final BirdDroppingFoodRecipe INSTANCE = new BirdDroppingFoodRecipe();
    public static final RecipeSerializer<BirdDroppingFoodRecipe> SERIALIZER =
            new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    public BirdDroppingFoodRecipe() { }

    @Override
    public boolean matches(CraftingInput input, @NotNull Level level) {
        return !this.assemble(input).isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input) {
        ItemStack food = ItemStack.EMPTY;
        ItemStack dropping = ItemStack.EMPTY;
        int occupiedSlots = 0;

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            occupiedSlots++;
            if (PrankFoodUtil.isDropping(stack)) {
                if (!dropping.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                dropping = stack;
            } else if (PrankFoodUtil.isEligibleFood(stack)) {
                if (!food.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                food = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (occupiedSlots != 2 || food.isEmpty() || dropping.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return PrankFoodUtil.makePrankFood(food, dropping);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
