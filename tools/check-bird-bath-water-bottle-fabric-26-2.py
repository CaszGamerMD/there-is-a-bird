#!/usr/bin/env python3
"""Keep bird bath plain-water-only bottle handling intact while removing PotionUtils."""

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
assert current == expected, "Bird bath water, sugar, food, cleanliness or splash behavior changed"
assert "PotionUtils" not in current

print("PASS: 26.2 potion data rejects effect-bearing bottles and accepts plain water only")
print("PASS: all original bird bath sweetening, refilling, cleaning and item-remainder rules retained")
