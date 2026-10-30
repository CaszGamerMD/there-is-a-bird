#!/usr/bin/env python3
"""Guard original Nikon clean-shot HUD and blocking overlay behavior on 26.2."""
from pathlib import Path
import subprocess

ROOT=Path(__file__).resolve().parents[1]
CAMERA="src/main/java/EdDYON/guaniao/client/camera/CameraClientCapture.java"
KEYS="src/main/java/EdDYON/guaniao/client/camera/CameraKeyMappings.java"
old=subprocess.check_output(["git","show","origin/main:"+CAMERA],cwd=ROOT,text=True).replace("\r\n","\n")
capture=(ROOT/CAMERA).read_text("utf-8")
keys=(ROOT/KEYS).read_text("utf-8")
proof=(ROOT/"tools/port-probes/GuaniaoCameraHudVisibilityProof.java").read_text("utf-8")
gradle=(ROOT/"build.gradle").read_text("utf-8")
ci=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")

for token in (
    "storedHideGui = minecraft.options.hideGui;",
    "minecraft.options.hideGui = true;",
    "minecraft.options.hideGui = storedHideGui;",
):
    assert token in old, "The original shutter HUD behavior is missing: "+token

for token in (
    "storedHideGui = minecraft.gui.hud.isHidden();",
    "setHudHidden(minecraft, true);",
    "setHudHidden(minecraft, storedHideGui);",
    "if (minecraft.gui.hud.isHidden() != hidden)",
    "minecraft.gui.hud.toggle();",
):
    assert token in capture, token
assert capture.index("storedHideGui = minecraft.gui.hud.isHidden();") < capture.index("setHudHidden(minecraft, true);")
assert capture.index("setHudHidden(minecraft, storedHideGui);") < capture.index("restoreSensitivity(minecraft);")
assert "minecraft.options.hideGui" not in capture
assert "minecraft.gui.overlay() != null" in capture
assert "minecraft.gui.overlay() == null" in capture
assert "minecraft.getOverlay()" not in capture
assert "minecraft.gui.overlay() != null" in keys
assert "minecraft.getOverlay()" not in keys
assert "GuaniaoCameraHudVisibilityProof.java" in gradle
assert "check-camera-hud-26-2.py" in ci
for token in ("minecraft.gui.hud.isHidden()", "minecraft.gui.hud.toggle()", "minecraft.gui.overlay()"):
    assert token in proof, token
print("PASS: original F1 preference preserved; 26.2 HUD/overlay access; no obsolete Options.hideGui")
