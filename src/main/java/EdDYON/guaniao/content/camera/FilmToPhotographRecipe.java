package EdDYON.guaniao.content.camera;

import EdDYON.guaniao.registry.GuaniaoItems;
import EdDYON.guaniao.registry.GuaniaoRecipeSerializers;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class FilmToPhotographRecipe extends CustomRecipe {
    public static final FilmToPhotographRecipe INSTANCE = new FilmToPhotographRecipe();
    public static final RecipeSerializer<FilmToPhotographRecipe> SERIALIZER =
            new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    public FilmToPhotographRecipe() { }

    @Override
    public boolean matches(CraftingInput input, @NotNull Level level) {
        return input.width() == 3 && input.height() == 3 && !this.findFilm(input).isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input) {
        ItemStack film = this.findFilm(input);
        if (film.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(GuaniaoItems.PHOTOGRAPH.get());
        PhotographData.copyImage(film, result);
        if (film.hasCustomHoverName()) {
            result.setHoverName(Component.translatable("item.guaniao.photograph.named", film.getHoverName()));
        }
        return result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private ItemStack findFilm(CraftingInput input) {
        if (input.width() != 3 || input.height() != 3) {
            return ItemStack.EMPTY;
        }

        ItemStack film = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (slot == 4) {
                if (!stack.is(GuaniaoItems.FILM.get()) || !PhotographData.hasImage(stack)) {
                    return ItemStack.EMPTY;
                }
                film = stack;
            } else if (!stack.is(Items.STICK)) {
                return ItemStack.EMPTY;
            }
        }
        return film;
    }
}
