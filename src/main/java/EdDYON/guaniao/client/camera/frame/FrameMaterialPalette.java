package EdDYON.guaniao.client.camera.frame;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.StainedGlassPaneBlock;

/** Built from the live registry, so installed mods and translated item names work too. */
final class FrameMaterialPalette {
    enum Kind { FRAME, TRIM, LINING, STICKER }
    enum Category { ALL, WOOD, STONE, GLASS, COLORED, NATURE, BUILDING, EQUIPMENT, FOOD, OTHER }
    private static final List<String> COLORS=List.of("white","light_gray","gray","black","brown","red",
            "orange","yellow","lime","green","cyan","light_blue","blue","purple","magenta","pink");
    private static final List<String> STONE_FAMILIES=List.of("stone","cobblestone","granite","diorite","andesite",
            "deepslate","tuff","calcite","basalt","blackstone","sandstone","quartz","brick","bricks",
            "prismarine","netherrack","end_stone","purpur","obsidian","bedrock");
    private record Entry(ItemStack stack,Category category,Identifier id,String family,int color) {}

    static List<ItemStack> entries(Kind kind,String query) {
        return entries(kind,Category.ALL,query);
    }
    static List<ItemStack> entries(Kind kind,Category category,String query) {
        String search=query.strip().toLowerCase(Locale.ROOT);
        return BuiltInRegistries.ITEM.stream().filter(item->allowed(kind,item))
                .filter(item->search.isEmpty() || new ItemStack(item).getHoverName().getString().toLowerCase(Locale.ROOT).contains(search)
                        || BuiltInRegistries.ITEM.getKey(item).toString().contains(search))
                .map(item->{
                    ItemStack stack=new ItemStack(item);Identifier id=BuiltInRegistries.ITEM.getKey(item);
                    int color=colorIndex(id.getPath());
                    String family=color<0 ? id.getPath() : id.getPath().substring(COLORS.get(color).length()+1);
                    return new Entry(stack,category(stack),id,family,color);
                })
                .filter(entry->category==Category.ALL || entry.category()==category)
                .sorted(Comparator.comparingInt((Entry entry)->entry.category().ordinal())
                        .thenComparingInt(entry->entry.id().getNamespace().equals("minecraft") ? 0 : 1)
                        .thenComparing(entry->entry.id().getNamespace())
                        .thenComparing(Entry::family).thenComparingInt(Entry::color)
                        .thenComparing(entry->entry.id().toString()))
                .map(Entry::stack).toList();
    }
    static List<Category> categories(Kind kind) {
        EnumSet<Category> categories=EnumSet.of(Category.ALL);
        BuiltInRegistries.ITEM.stream().filter(item->allowed(kind,item))
                .forEach(item->categories.add(category(new ItemStack(item))));
        return List.copyOf(categories);
    }
    static Category category(ItemStack stack) {
        Item item=stack.getItem();String path=BuiltInRegistries.ITEM.getKey(item).getPath();
        if(stack.has(DataComponents.FOOD) || item instanceof PotionItem || item instanceof MilkBucketItem
                || item instanceof BlockItem block && block.getBlock() instanceof CakeBlock)return Category.FOOD;
        if(item instanceof BlockItem block) {
            var state=block.getBlock().defaultBlockState();
            if(block.getBlock() instanceof AbstractGlassBlock || block.getBlock() instanceof StainedGlassPaneBlock
                    || path.contains("glass") || path.endsWith("_pane"))
                return Category.GLASS;
            if(state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS) || state.is(BlockTags.WOODEN_BUTTONS)
                    || state.is(BlockTags.WOODEN_DOORS) || state.is(BlockTags.WOODEN_FENCES)
                    || state.is(BlockTags.WOODEN_PRESSURE_PLATES) || state.is(BlockTags.WOODEN_SLABS)
                    || state.is(BlockTags.WOODEN_STAIRS) || state.is(BlockTags.WOODEN_TRAPDOORS)
                    || state.is(BlockTags.FENCE_GATES) || path.startsWith("bamboo_mosaic"))return Category.WOOD;
            if(block.getBlock() instanceof BushBlock || block.getBlock() instanceof LeavesBlock
                    || state.is(BlockTags.FLOWERS) || state.is(BlockTags.LEAVES))return Category.NATURE;
            if(state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER)
                    || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.IRON_ORES)
                    || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.REDSTONE_ORES)
                    || state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.COAL_ORES)
                    || state.is(BlockTags.EMERALD_ORES) || state.is(BlockTags.COPPER_ORES)
                    || path.endsWith("_ore") || STONE_FAMILIES.stream().anyMatch(stone->family(path,stone)))return Category.STONE;
            if(colorIndex(path)>=0)return Category.COLORED;
            return Category.BUILDING;
        }
        if(stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(ItemTags.HOES)
                || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HEAD_ARMOR) || stack.is(ItemTags.CHEST_ARMOR)
                || stack.is(ItemTags.LEG_ARMOR) || stack.is(ItemTags.FOOT_ARMOR)
                || stack.isDamageableItem() || item==Items.COMPASS || item==Items.RECOVERY_COMPASS
                || item==Items.CLOCK || item==Items.SPYGLASS)return Category.EQUIPMENT;
        return Category.OTHER;
    }
    private static boolean family(String path,String family) {
        return path.equals(family) || path.startsWith(family+"_") || path.endsWith("_"+family)
                || path.contains("_"+family+"_");
    }
    private static int colorIndex(String path) {
        for(int index=0;index<COLORS.size();index++)if(path.startsWith(COLORS.get(index)+"_"))return index;
        return -1;
    }
    private static boolean allowed(Kind kind,Item item) {
        if(item==Items.AIR) return false;
        if(kind==Kind.STICKER) return true;
        if(kind==Kind.LINING && item==Items.PAPER) return true;
        return item instanceof BlockItem block && block.getBlock().defaultBlockState().getRenderShape()!=RenderShape.INVISIBLE;
    }
    static ItemStack stack(Identifier id) {
        // A saved design may refer to an optional mod that's no longer installed.
        // Preserve the editor's original missing-material fallback instead of
        // making the palette crash while browsing that design.
        Item item=BuiltInRegistries.ITEM.getValue(id);
        return new ItemStack(item==null ? Items.AIR : item);
    }
}
