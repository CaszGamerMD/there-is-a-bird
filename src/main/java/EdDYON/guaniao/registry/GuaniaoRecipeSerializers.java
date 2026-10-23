package EdDYON.guaniao.registry;

import EdDYON.guaniao.content.camera.FilmToPhotographRecipe;
import EdDYON.guaniao.content.camera.PhotographFrameRecipe;
import EdDYON.guaniao.content.dropping.BirdDroppingFoodRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import EdDYON.guaniao.fabric.FabricRegistryQueue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public final class GuaniaoRecipeSerializers {
    public static final FabricRegistryQueue<RecipeSerializer<?>> RECIPE_SERIALIZERS = new FabricRegistryQueue<>(BuiltInRegistries.RECIPE_SERIALIZER, "guaniao");
    public static final FabricRegistryQueue.Ref<RecipeSerializer<FilmToPhotographRecipe>> FILM_TO_PHOTOGRAPH = RECIPE_SERIALIZERS.register("film_to_photograph",
            () -> new SimpleCraftingRecipeSerializer<>(FilmToPhotographRecipe::new));
    public static final FabricRegistryQueue.Ref<RecipeSerializer<PhotographFrameRecipe>> PHOTOGRAPH_FRAME = RECIPE_SERIALIZERS.register("photograph_frame",
            PhotographFrameRecipe.Serializer::new);
    public static final FabricRegistryQueue.Ref<RecipeSerializer<BirdDroppingFoodRecipe>> BIRD_DROPPING_FOOD = RECIPE_SERIALIZERS.register("bird_dropping_food",
            () -> new SimpleCraftingRecipeSerializer<>(BirdDroppingFoodRecipe::new));

    private GuaniaoRecipeSerializers() {
    }
}
