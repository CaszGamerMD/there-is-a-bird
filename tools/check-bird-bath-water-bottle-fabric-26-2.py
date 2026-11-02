#!/usr/bin/env python3
"""Keep authored bird bath behavior intact across 26.2 potion, block and codec APIs."""

from pathlib import Path
import subprocess


FILE = "src/main/java/EdDYON/guaniao/content/bath/BirdBathBlock.java"
original = subprocess.check_output(["git", "show", f"origin/main:{FILE}"], text=True).replace("\r\n", "\n")
current = Path(FILE).read_text(encoding="utf-8").replace("\r\n", "\n")

expected = original
# Prior Fabric client block-break effect move; now handled in the client registration.
expected = expected.replace("import EdDYON.guaniao.client.particle.PlaceableBlockBreakEffects;\n", "")
expected = expected.replace("import net.minecraftforge.client.extensions.common.IClientBlockExtensions;\n", "")
expected = expected.replace("import java.util.function.Consumer;\n", "")
expected = expected.replace(
    "    @Override\n    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {\n"
    "        consumer.accept(PlaceableBlockBreakEffects.birdBath());\n    }\n\n", "")
expected = expected.replace("import net.minecraft.core.BlockPos;", "import net.minecraft.core.BlockPos;\nimport net.minecraft.core.component.DataComponents;")
expected = expected.replace("import net.minecraft.world.item.alchemy.PotionUtils;", "import net.minecraft.world.item.alchemy.PotionContents;")
expected = expected.replace(
    "return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER\n"
    "                && PotionUtils.getCustomEffects(stack).isEmpty();",
    "PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);\n"
    "        return stack.is(Items.POTION) && contents != null && contents.is(Potions.WATER)\n"
    "                && contents.customEffects().isEmpty();",
)


def migrate(old: str, new: str) -> None:
    global expected
    assert old in expected, f"Original bath source missing expected section: {old[:80]}"
    expected = expected.replace(old, new)


migrate("import EdDYON.guaniao.registry.GuaniaoBlockEntityTypes;",
        "import EdDYON.guaniao.registry.GuaniaoBlockEntityTypes;\n"
        "import com.mojang.serialization.MapCodec;\n"
        "import com.mojang.serialization.codecs.RecordCodecBuilder;")
migrate("public class BirdBathBlock extends BaseEntityBlock {",
        "public class BirdBathBlock extends BaseEntityBlock {\n"
        "    public static final MapCodec<BirdBathBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(\n"
        "            BirdBathVariant.CODEC.fieldOf(\"variant\").forGetter(BirdBathBlock::variant),\n"
        "            propertiesCodec()\n"
        "    ).apply(instance, BirdBathBlock::new));\n")
migrate("    public BirdBathVariant variant() {\n"
        "        return this.variant;\n"
        "    }",
        "    public BirdBathVariant variant() {\n"
        "        return this.variant;\n"
        "    }\n\n"
        "    @Override\n"
        "    protected MapCodec<? extends BaseEntityBlock> codec() {\n"
        "        return CODEC;\n"
        "    }")
migrate("        return RenderShape.ENTITYBLOCK_ANIMATED;",
        "        // GeckoLib renders the block entity; Minecraft 26.2 has no ENTITYBLOCK_ANIMATED.\n"
        "        return RenderShape.INVISIBLE;")
migrate("    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {",
        "    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,\n"
        "                                          Player player, InteractionHand hand, BlockHitResult hit) {\n"
        "        return useBath(level, pos, player, hand, held);\n"
        "    }\n\n"
        "    @Override\n"
        "    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,\n"
        "                                               Player player, BlockHitResult hit) {\n"
        "        return useBath(level, pos, player, InteractionHand.MAIN_HAND, ItemStack.EMPTY);\n"
        "    }\n\n"
        "    private InteractionResult useBath(Level level, BlockPos pos, Player player,\n"
        "                                      InteractionHand hand, ItemStack stack) {")
migrate("        ItemStack stack = player.getItemInHand(hand);\n", "")
migrate("    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {",
        "    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {")
migrate("        super.playerWillDestroy(level, pos, state, player);",
        "        return super.playerWillDestroy(level, pos, state, player);")
migrate("InteractionResult.sidedSuccess(level.isClientSide)", "sidedSuccess(level)")
migrate("level.isClientSide", "level.isClientSide()")
migrate("    private static void giveOrReplaceHeldItem(Player player, InteractionHand hand, ItemStack replacement) {",
        "    private static InteractionResult sidedSuccess(Level level) {\n"
        "        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;\n"
        "    }\n\n"
        "    private static void giveOrReplaceHeldItem(Player player, InteractionHand hand, ItemStack replacement) {")

assert current == expected, "Bird bath water, sugar, food, cleanliness or splash behavior changed"
assert "PotionUtils" not in current

variant_file = "src/main/java/EdDYON/guaniao/content/bath/BirdBathVariant.java"
variant_original = subprocess.check_output(["git", "show", f"origin/main:{variant_file}"], text=True).replace("\r\n", "\n")
variant_expected = variant_original
# Prior Forge ResourceLocation -> 26.2 Identifier port already completed for variant assets.
variant_expected = variant_expected.replace("ResourceLocation", "Identifier")
variant_expected = variant_expected.replace("new Identifier(GuaniaoMod.MOD_ID,", "Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID,")
variant_expected = variant_expected.replace("import EdDYON.guaniao.GuaniaoMod;",
    "import EdDYON.guaniao.GuaniaoMod;\nimport com.mojang.serialization.Codec;")
variant_expected = variant_expected.replace("import net.minecraft.resources.Identifier;",
    "import net.minecraft.resources.Identifier;\nimport net.minecraft.util.StringRepresentable;")
variant_expected = variant_expected.replace("public enum BirdBathVariant {",
    "public enum BirdBathVariant implements StringRepresentable {")
variant_expected = variant_expected.replace("    private final String id;",
    "    public static final Codec<BirdBathVariant> CODEC = StringRepresentable.fromEnum(BirdBathVariant::values);\n\n"
    "    private final String id;")
variant_expected = variant_expected.replace("    public String id() {\n        return this.id;\n    }",
    "    public String id() {\n        return this.id;\n    }\n\n"
    "    @Override\n    public String getSerializedName() {\n        return this.id;\n    }")
assert Path(variant_file).read_text(encoding="utf-8").replace("\r\n", "\n") == variant_expected, "Bird bath variants or model mappings changed"

print("PASS: 26.2 potion data rejects effect-bearing bottles and accepts plain water only")
print("PASS: six bath variants retain original IDs, geometries and assets through the 26.2 codec")
print("PASS: 26.2 block hooks preserve original sweetening, refilling, cleaning and item-remainder rules")
