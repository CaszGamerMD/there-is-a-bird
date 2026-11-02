#!/usr/bin/env python3
"""Preserve bath content, dirt and tint decisions while swapping GeckoLib bone mutation."""

from pathlib import Path
import subprocess


FILE = "src/main/java/EdDYON/guaniao/client/bath/BirdBathBoneVisibility.java"
original = subprocess.check_output(["git", "show", f"origin/main:{FILE}"], text=True).replace("\r\n", "\n")
current = Path(FILE).read_text(encoding="utf-8").replace("\r\n", "\n")

expected = original.replace("import software.bernie.geckolib.cache.object.GeoBone;",
                            "import com.geckolib.animation.state.BoneSnapshot;")
expected = expected.replace("GeoBone bone)", "BoneSnapshot bone)")
expected = expected.replace("bone.getName()", "bone.getBone().name()")
expected = expected.replace("bone.setHidden(!visible)", "bone.skipRender(!visible)")
expected = expected.replace("bone.setChildrenHidden(!visible)", "bone.skipChildrenRender(!visible)")
assert current == expected, "Bath contents, dirt coverage, water tints or spoilage logic changed"

print("PASS: original 23 water/food/dirt bones and all authored tint colors preserved")
print("PASS: 26.2 GeckoLib 5 per-frame bone snapshots receive the same visibility decisions")
