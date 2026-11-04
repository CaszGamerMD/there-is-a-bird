#!/usr/bin/env python3
"""Ensure 26.2 biome tag renaming did not disturb the long-flight migration goal."""

from pathlib import Path
import subprocess


FILE = "src/main/java/EdDYON/guaniao/content/bird/brain/BirdMigrationGoal.java"
original = subprocess.check_output(["git", "show", f"origin/main:{FILE}"], text=True).replace("\r\n", "\n")
current = Path(FILE).read_text(encoding="utf-8").replace("\r\n", "\n")

expected = original.replace("import javax.annotation.Nullable;", "import org.jetbrains.annotations.Nullable;")
expected = expected.replace("import net.minecraft.resources.ResourceLocation;", "import net.minecraft.resources.Identifier;")
expected = expected.replace(
    'new ResourceLocation("guaniao", species.id() + "_habitat")',
    'Identifier.fromNamespaceAndPath("guaniao", species.id() + "_habitat")',
)
assert expected != original, "Expected legacy Minecraft identifier baseline"
assert current == expected, "Long-distance migration behavior changed beyond 26.2 identifier substitution"

print("PASS: migration biome tags use 26.2 Identifier; all target, steering and stall-recovery behavior unchanged")
