#!/usr/bin/env python3
"""Original camera FOV conversion: Forge viewport event -> 26.2 world Camera."""
from pathlib import Path
import json
import subprocess

ROOT=Path(__file__).resolve().parent.parent
CAM="src/main/java/EdDYON/guaniao/client/camera/CameraClientCapture.java"
MIX="src/main/java/EdDYON/guaniao/mixin/GuaniaoCameraFieldOfViewMixin.java"
before=subprocess.check_output(["git","show","origin/main:"+CAM],cwd=ROOT,text=True).replace("\r\n","\n")
capture=(ROOT/CAM).read_text("utf-8")
mixin=(ROOT/MIX).read_text("utf-8")
config=json.loads((ROOT/"src/main/resources/guaniao.mixins.json").read_text("utf-8"))
workflow=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
math=(ROOT/"src/main/java/EdDYON/guaniao/client/camera/CameraLensMath.java").read_text("utf-8")

for token in (
    "public static void modifyFov(ViewportEvent.ComputeFov event)",
    "if (!event.usedConfiguredFov())",
    "event.setFOV(currentFov())",
    "event.setFOV(pendingCaptureFov)"
):
    assert token in before, "Missing original source behavior: "+token

for token in (
    "@Mixin(Camera.class)",
    'method = "calculateFov(F)F"',
    '@At("RETURN"), require = 1',
    "CameraClientCapture.overrideWorldFov(vanillaFov)"
):
    assert token in mixin, token

assert "GuaniaoCameraFieldOfViewMixin" in config["client"]
assert "GuaniaoCameraFieldOfViewMixin" not in config.get("mixins",[])
assert "net.minecraftforge.client.event.ViewportEvent" not in capture
assert "modifyFov(ViewportEvent.ComputeFov" not in capture
assert "public static float overrideWorldFov(float vanillaFov)" in capture
assert "if (viewfinderOpen) return (float) currentFov();" in capture
assert "if (cleanCapturePending) return (float) pendingCaptureFov;" in capture
assert "return vanillaFov;" in capture
assert "private static double currentFov()" in capture
assert "CameraLensMath.focalLengthToFov(focalLength)" in capture
assert "FULL_FRAME_SENSOR_WIDTH = 36.0D" in math
assert "Camera.calculateHudFov" in mixin and "calculateHudFov(F)F" not in mixin
assert "check-camera-fov-26-2.py" in workflow
print("PASS: 26.2 world FOV retains Nikon lens and frozen clean capture; vanilla is unchanged when inactive")
