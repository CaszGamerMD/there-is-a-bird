#!/usr/bin/env python3
"""Keep the three authored feather-enchantment books identical apart from 26.2 tooltip signatures."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
BASE = "src/main/java/EdDYON/guaniao/content/enchantment/"

for name in ("BurialPlumeBookItem", "HuntingReturnBookItem", "RivenPlumeBookItem"):
    path = BASE + name + ".java"
    original = subprocess.check_output(["git", "show", "origin/main:" + path], cwd=ROOT, text=True)
    expected = original.replace("import java.util.List;", "import java.util.function.Consumer;")
    expected = expected.replace("import net.minecraft.world.level.Level;",
                                "import net.minecraft.world.item.component.TooltipDisplay;")
    expected = expected.replace("import org.jetbrains.annotations.Nullable;\n", "")
    expected = expected.replace(
        "public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {",
        "public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {")
    expected = expected.replace("tooltip.add(Component.translatable(",
                                "tooltip.accept(Component.translatable(")
    actual = (ROOT / path).read_text(encoding="utf-8")
    assert actual == expected, f"{name}: changed original foil behavior, tooltip key, styling or more"
    assert "return true;" in actual.split("boolean isFoil(ItemStack stack)")[1].split("}", 1)[0]
    assert actual.count("tooltip.accept(Component.translatable(") == 1
    print(f"PASS: {name} has identical foil, localization and styling")
