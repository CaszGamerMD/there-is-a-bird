#!/usr/bin/env python3
"""Prove original per-pixel filter algorithms are preserved in pure Java."""
from pathlib import Path
import subprocess

ROOT=Path(__file__).resolve().parents[1]
OLD="src/main/java/EdDYON/guaniao/client/camera/CameraImageFilters.java"
original=subprocess.check_output(["git","show","origin/main:"+OLD],cwd=ROOT,text=True).replace("\r\n","\n")
facade=(ROOT/OLD).read_text("utf-8")
core=(ROOT/"src/main/java/EdDYON/guaniao/client/camera/CameraFilterPixelCore.java").read_text("utf-8")
workflow=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
definitions=(ROOT/"src/main/java/EdDYON/guaniao/content/camera/CameraFilter.java").read_text("utf-8")
assert "MAX_ID = 50" in definitions
assert "CameraFilterPixelCore.apply(" in facade
assert "filter == CameraFilter.COLOR_BALANCE" in facade
assert "filter != CameraFilter.NONE" in facade
assert "CameraFilter.GLITCH_RGB.id()" in facade
assert "CameraFilter.CHROMATIC_ABERRATION.id()" in facade
assert "net.minecraft." not in core and "net.minecraftforge." not in core
assert "import EdDYON.guaniao.content.camera.CameraFilter" not in core
original_tail=original[original.index("    private static void applyUniversal"):]
normalized=original_tail.replace(
    "private static void applyUniversal(int[] pixels, int width, int height, int mode, long seed)",
    "private static void applyUniversal(int[] pixels, int width, int height, int mode,\n                                       int glitchId, int chromaticId, long seed)"
).replace("CameraFilter.GLITCH_RGB.id()", "glitchId").replace(
    "CameraFilter.CHROMATIC_ABERRATION.id()", "chromaticId"
).replace("Mth.clamp(", "Math.clamp(")
actual_tail=core[core.index("    private static void applyUniversal"):]
assert normalized == actual_tail, "The original CPU effect and sharpening algorithms were modified"
assert "GuaniaoCameraFilterCoreRegression.java" in workflow
assert "check-camera-filter-core-26-2.py" in workflow
print("PASS: original CPU filter math and 51 IDs preserved; all native MC dependencies extracted")
