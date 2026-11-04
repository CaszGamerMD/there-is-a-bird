#!/usr/bin/env python3
"""Guarantee the original frame style IDs and values survive item NBT component migration."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
path = "src/main/java/EdDYON/guaniao/content/camera/PhotographFrameStyle.java"
old = subprocess.check_output(["git", "show", "origin/main:" + path], cwd=root, text=True)
new = (root / path).read_text("utf-8")
gradle = (root / "build.gradle").read_text("utf-8")

original_gameplay = old.split("    public PhotographFrameStyle withShape(Shape shape) {")[1]
ported_gameplay = new.split("    public PhotographFrameStyle withShape(Shape shape) {")[1]
assert original_gameplay == ported_gameplay, "frame geometry variants, IDs or material choices changed"

for token in (
    'public static final String TAG = "PhotoFrame";',
    "getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()",
    "custom.get(TAG) instanceof CompoundTag tag",
    'tag.getStringOr("Shape", "")',
    'tag.getStringOr("Material", "")',
    'tag.getStringOr("Ornament", "")',
    "CustomData.update(DataComponents.CUSTOM_DATA, stack, custom -> custom.put(TAG, tag))",
    'tag.putString("Shape", style.shape.id())',
    'tag.putString("Material", style.material.id())',
    'tag.putString("Ornament", style.ornament.id())',
):
    assert token in new, "missing original style storage or 26.2 component API: " + token
for old_api in ("getTagElement(", "getOrCreateTag(", 'tag.getString("'):
    assert old_api not in new, "legacy style NBT accessor: " + old_api
assert "include 'EdDYON/guaniao/content/camera/PhotographFrameStyle.java'" in gradle
print("PASS: all original frame shapes, materials, ornaments and style IDs unchanged")
print("PASS: original PhotoFrame compound now stored in custom item data components")
print("PASS: real Java 25 photograph style compilation included in focused gate")
