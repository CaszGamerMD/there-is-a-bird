#!/usr/bin/env python3
"""Validate exact original lens and thumbnail crop equations after extraction."""
from pathlib import Path
import subprocess

ROOT=Path(__file__).resolve().parents[1]
path="src/main/java/EdDYON/guaniao/client/camera/CameraPreviewPostEffect.java"
old=subprocess.check_output(["git","show","origin/main:"+path],cwd=ROOT,text=True).replace("\r\n","\n")
new=(ROOT/path).read_text("utf-8")
uv=(ROOT/"src/main/java/EdDYON/guaniao/client/camera/CameraPreviewUv.java").read_text("utf-8")
workflow=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
for formula in (
    "float u0 = (float)left / guiWidth;",
    "float u1 = (float)right / guiWidth;",
    "float vTop = 1.0F - (float)top / guiHeight;",
    "float vBottom = 1.0F - (float)bottom / guiHeight;",
    "float visibleWidth = destinationAspect / sourceAspect;",
    "float visibleHeight = sourceAspect / destinationAspect;",
    "float crop = (1.0F - visibleWidth) * 0.5F;",
    "float crop = (1.0F - visibleHeight) * 0.5F;",
    "u0 = crop;", "u1 = 1.0F - crop;",
    "vBottom = crop;", "vTop = 1.0F - crop;"
):
    assert formula in old and formula in uv, formula
assert "CameraPreviewUv.lens(left, top, right, bottom, guiWidth, guiHeight)" in new
assert "CameraPreviewUv.centerCrop(" in new
assert new.count("uv.u0(), uv.vBottom(), uv.u1(), uv.vTop()") == 2
assert "minecraft.gui.screen() instanceof CameraFilterPickerScreen screen" in new
assert "net.minecraft." not in uv and "net.minecraftforge." not in uv
assert "GuaniaoCameraPreviewUvRegression.java" in workflow
assert "check-camera-preview-uv-26-2.py" in workflow
print("PASS: same author camera lens, flipped vertical UVs and aspect crop; screen accessor ported")
