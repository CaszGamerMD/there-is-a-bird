#!/usr/bin/env python3
"""Preserve bath selection, stand position, splash timing and hop motion on Fabric 26.2."""

from pathlib import Path
import subprocess


FILE = "src/main/java/EdDYON/guaniao/content/bath/BirdBathUseGoal.java"
original = subprocess.check_output(["git", "show", f"origin/main:{FILE}"], text=True).replace("\r\n", "\n")
expected = original.replace("this.bird.level().isClientSide", "this.bird.level().isClientSide()")
assert original.count("this.bird.hasImpulse = true;") == 2
# Entity.hasImpulse was removed; hurtMarked requests the current 26.2 velocity update.
expected = expected.replace("this.bird.hasImpulse = true;", "this.bird.hurtMarked = true;")
current = Path(FILE).read_text(encoding="utf-8").replace("\r\n", "\n")
assert current == expected, "Bath bird AI movement, occupation or splash lifecycle drifted from the authored source"

print("PASS: original bird bath search, pathing, bath use, hop and settle sequences preserved")
print("PASS: 26.2 bird hop and settle motion request a velocity sync")
