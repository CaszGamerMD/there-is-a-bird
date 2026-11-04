package EdDYON.guaniao.fabric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

/** Compile-only proof of Minecraft 26.2 recipe codecs and author-equivalent shaped delegation. */
public final class GuaniaoRecipeCodecProof implements CraftingRecipe {
    public static final MapCodec<GuaniaoRecipeCodecProof> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    ShapedRecipe.MAP_CODEC.forGetter(recipe -> recipe.shaped),
                    Codec.STRING.fieldOf("frame_shape").forGetter(recipe -> recipe.shape)
            ).apply(i, GuaniaoRecipeCodecProof::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, GuaniaoRecipeCodecProof> STREAM_CODEC =
            StreamCodec.composite(
                    ShapedRecipe.STREAM_CODEC, recipe -> recipe.shaped,
                    ByteBufCodecs.STRING_UTF8, recipe -> recipe.shape,
                    GuaniaoRecipeCodecProof::new);
    public static final RecipeSerializer<GuaniaoRecipeCodecProof> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
    private final ShapedRecipe shaped;
    private final String shape;

    public GuaniaoRecipeCodecProof(ShapedRecipe shaped, String shape) {
        this.shaped = shaped;
        this.shape = shape;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return shaped.matches(input, level);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return shaped.assemble(input);
    }

    @Override
    public RecipeSerializer<GuaniaoRecipeCodecProof> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean showNotification() {
        return shaped.showNotification();
    }

    @Override
    public String group() {
        return shaped.group();
    }

    @Override
    public CraftingBookCategory category() {
        return shaped.category();
    }

    @Override
    public PlacementInfo placementInfo() {
        return shaped.placementInfo();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapedCraftingRecipeDisplay(
                shaped.getWidth(), shaped.getHeight(),
                shaped.getIngredients().stream()
                        .map(value -> (SlotDisplay) value.map(Ingredient::display)
                                .orElse(SlotDisplay.Empty.INSTANCE))
                        .toList(),
                new SlotDisplay.ItemStackSlotDisplay(
                        ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.PAPER))),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    public static final class SimpleProof extends CustomRecipe {
        public static final SimpleProof INSTANCE = new SimpleProof();
        public static final RecipeSerializer<SimpleProof> SERIALIZER = new RecipeSerializer<>(
                MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

        @Override
        public boolean matches(CraftingInput input, Level level) { return true; }

        @Override
        public ItemStack assemble(CraftingInput input) { return ItemStack.EMPTY; }

        @Override
        public RecipeSerializer<SimpleProof> getSerializer() { return SERIALIZER; }
    }
}
