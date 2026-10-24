package EdDYON.guaniao.content.camera;

import EdDYON.guaniao.registry.GuaniaoItems;
import EdDYON.guaniao.registry.GuaniaoRecipeSerializers;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

/**
 * The original shaped crafting recipe, including the preview and all original
 * photo contents, with 26.2's immutable recipe serializer API.
 *
 * We delegate to vanilla ShapedRecipe rather than extending it: in 26.2 a
 * ShapedRecipe's typed serializer return cannot be covariantly overridden by
 * a recipe that has its own frame_shape field.
 */
public final class PhotographFrameRecipe implements CraftingRecipe {
    public static final MapCodec<PhotographFrameRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ShapedRecipe.MAP_CODEC.forGetter(recipe -> recipe.shaped),
                    Codec.STRING.fieldOf("frame_shape").forGetter(recipe -> recipe.shape.id())
            ).apply(instance, PhotographFrameRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PhotographFrameRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ShapedRecipe.STREAM_CODEC, recipe -> recipe.shaped,
                    ByteBufCodecs.STRING_UTF8, recipe -> recipe.shape.id(),
                    PhotographFrameRecipe::new);
    public static final RecipeSerializer<PhotographFrameRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final ShapedRecipe shaped;
    private final PhotographFrameStyle.Shape shape;

    public PhotographFrameRecipe(ShapedRecipe shaped, String shapeId) {
        this.shaped = shaped;
        this.shape = shape(shapeId);
    }

    private static PhotographFrameStyle.Shape shape(String id) {
        if (id == null || id.length() > 32) {
            throw new IllegalArgumentException("Invalid photograph frame shape");
        }
        for (PhotographFrameStyle.Shape shape : PhotographFrameStyle.Shape.values()) {
            if (shape.id().equals(id)) return shape;
        }
        throw new IllegalArgumentException("Unknown photograph frame shape: " + id);
    }

    private static ItemStack preview(PhotographFrameStyle.Shape shape) {
        ItemStack stack = new ItemStack(GuaniaoItems.PHOTOGRAPH.get());
        PhotographFrameStyle.write(stack, PhotographFrameStyle.read(stack).withShape(shape));
        return stack;
    }

    private ItemStack photograph(CraftingInput input) {
        ItemStack photograph = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (!stack.is(GuaniaoItems.PHOTOGRAPH.get())) continue;
            if (!photograph.isEmpty() || !PhotographData.hasImage(stack)) return ItemStack.EMPTY;
            photograph = stack;
        }
        return photograph;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack photo = photograph(input);
        return !photo.isEmpty() && PhotographFrameStyle.read(photo).shape() != shape
                && shaped.matches(input, level);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack source = photograph(input);
        if (source.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = source.copy();
        result.setCount(1);
        PhotographFrameStyle.write(result, PhotographFrameStyle.read(source).withShape(shape));
        return result;
    }

    @Override
    public RecipeSerializer<PhotographFrameRecipe> getSerializer() {
        return GuaniaoRecipeSerializers.PHOTOGRAPH_FRAME.get();
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
                shaped.getWidth(),
                shaped.getHeight(),
                shaped.getIngredients().stream()
                        .map(ingredient -> (SlotDisplay) ingredient.map(Ingredient::display)
                                .orElse(SlotDisplay.Empty.INSTANCE))
                        .toList(),
                new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(preview(shape))),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }
}
