#!/usr/bin/env python3
"""Guard original camera mode/lens defaults and seven NBT field IDs on Fabric."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
path = "src/main/java/EdDYON/guaniao/content/camera/CameraSettingsData.java"
original = subprocess.check_output(["git", "show", "origin/main:" + path], cwd=root, text=True)
current = (root / path).read_text("utf-8")
gradle = (root / "build.gradle").read_text("utf-8")

key_declarations = r'private static final String TAG_[A-Z_]+ = "[^"]+";'
assert re.findall(key_declarations, original) == re.findall(key_declarations, current)

for token in (
    "CustomData.update(DataComponents.CUSTOM_DATA, stack, tag ->",
    "stack.get(DataComponents.CUSTOM_DATA)", "data.copyTag()",
    "CameraFilter.NONE", "CameraState.defaults()",
    "CameraFilter.byId(tag.getIntOr(TAG_FILTER, 0))",
    "tag.contains(TAG_LENS)", "tag.contains(TAG_SHOOTING_MODE)",
    "tag.contains(TAG_FOCAL_LENGTH)", "tag.contains(TAG_APERTURE)",
    "tag.contains(TAG_FOCUS_MODE)", "tag.contains(TAG_FOCUS_DISTANCE)",
    "tag.putInt(TAG_FILTER, state.filter().id())",
    "tag.putInt(TAG_LENS, state.lens().id())",
    "tag.putInt(TAG_SHOOTING_MODE, state.shootingMode().id())",
    "tag.putDouble(TAG_FOCAL_LENGTH, state.focalLength())",
    "tag.putInt(TAG_APERTURE, state.aperture().id())",
    "tag.putInt(TAG_FOCUS_MODE, state.focusMode().id())",
    "tag.putDouble(TAG_FOCUS_DISTANCE, state.focusDistance())",
):
    assert token in current, "missing original camera field or 26.2 migration: " + token
for legacy in ("getOrCreateTag(", "getTag()", "tag.getInt(TAG_", "tag.getDouble(TAG_"):
    assert legacy not in current, "old camera NBT usage remains: " + legacy

for file in ("CameraSettingsData", "CameraLens", "CameraAperture", "CameraFocusMode",
             "CameraShootingMode", "CameraState"):
    assert "include 'EdDYON/guaniao/content/camera/" + file + ".java'" in gradle
print("PASS: seven original camera setting keys and conditional defaults retained")
print("PASS: Fabric 26.2 custom item components retain lens, mode and focus choices")
print("PASS: complete real Java 25 camera settings component compile registered")
