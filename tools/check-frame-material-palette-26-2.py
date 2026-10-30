#!/usr/bin/env python3
"""Source-level parity for the original live-registry Workbench material selector."""
from pathlib import Path
import subprocess

ROOT=Path(__file__).resolve().parents[1]
PATH="src/main/java/EdDYON/guaniao/client/camera/frame/FrameMaterialPalette.java"
old=subprocess.check_output(["git","show","origin/main:"+PATH],cwd=ROOT,text=True).replace("\r\n","\n")
new=(ROOT/PATH).read_text("utf-8").replace("\r\n","\n")

# Original filter categories, colors, ordering and optional mod discovery.
for term in ('enum Kind { FRAME, TRIM, LINING, STICKER }',
             'enum Category { ALL, WOOD, STONE, GLASS, COLORED, NATURE, BUILDING, EQUIPMENT, FOOD, OTHER }',
             'static List<ItemStack> entries(Kind kind,Category category,String query)',
             'BuiltInRegistries.ITEM.stream().filter(item->allowed(kind,item))',
             'category==Category.ALL || entry.category()==category',
             'thenComparing(entry->entry.id().getNamespace())',
             'thenComparing(Entry::family).thenComparingInt(Entry::color)',
             'thenComparing(entry->entry.id().toString())',
             'COLORS=List.of("white","light_gray","gray","black","brown","red"',
             'STONE_FAMILIES=List.of("stone","cobblestone","granite"',
             'kind==Kind.STICKER', 'kind==Kind.LINING && item==Items.PAPER',
             'static List<Category> categories(Kind kind)', 'return Category.BUILDING;',
             'return Category.EQUIPMENT;', 'return Category.OTHER;'):
    assert term in old and term in new, term

# No Forge-only tags; 26.2 item components and the still-supported vanilla
# block/item tags determine the same material-picker categories.
for term in ('net.minecraft.resources.Identifier',
             'DataComponents.FOOD','stack.has(DataComponents.FOOD)',
             'BlockTags.BASE_STONE_OVERWORLD','BlockTags.BASE_STONE_NETHER',
             'BlockTags.GOLD_ORES','BlockTags.IRON_ORES','BlockTags.DIAMOND_ORES',
             'BlockTags.REDSTONE_ORES','BlockTags.LAPIS_ORES',
             'BlockTags.COAL_ORES','BlockTags.EMERALD_ORES','BlockTags.COPPER_ORES',
             'BlockTags.FENCE_GATES',
             'ItemTags.SWORDS','ItemTags.AXES','ItemTags.HOES',
             'ItemTags.PICKAXES','ItemTags.SHOVELS',
             'ItemTags.HEAD_ARMOR','ItemTags.CHEST_ARMOR',
             'ItemTags.LEG_ARMOR','ItemTags.FOOT_ARMOR',
             'new ItemStack(item).getHoverName().getString()',
             'BuiltInRegistries.ITEM.getValue(id)',
             'item==null ? Items.AIR : item'):
    assert term in new, term
for gone in ('net.minecraftforge.', 'ResourceLocation', 'Tags.Blocks.', 'Tags.Items.',
             'ItemTags.TOOLS', 'item.isEdible()', 'BuiltInRegistries.ITEM.get('):
    assert gone not in new, gone
print("FRAME WORKBENCH MATERIAL PALETTE 26.2 PARITY OK: live mod registry,"
      " original 10 categories, stable material ordering, saved-mod fallback")
