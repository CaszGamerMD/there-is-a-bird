#!/usr/bin/env python3
"""Check Fabric 26.2 camera mouse events preserve the original Forge behavior."""
from pathlib import Path
import json
import subprocess

ROOT = Path(__file__).resolve().parent.parent
PATH = "src/main/java/EdDYON/guaniao/mixin/GuaniaoCameraMouseInputMixin.java"
MIXIN = (ROOT / PATH).read_text("utf-8")
CAPTURE = (ROOT / "src/main/java/EdDYON/guaniao/client/camera/CameraClientCapture.java").read_text("utf-8")
CONFIG = json.loads((ROOT / "src/main/resources/guaniao.mixins.json").read_text("utf-8"))
WORKFLOW = (ROOT / ".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
OLD = subprocess.check_output([
    "git", "show",
    "origin/main:src/main/java/EdDYON/guaniao/client/camera/CameraClientEvents.java"
], cwd=ROOT, text=True).replace("\r\n", "\n")

for token in (
    "@Mixin(MouseHandler.class)",
    'method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V"',
    'method = "onScroll(JDD)V"',
    "cancellable = true, require = 1",
    "minecraft.getWindow().getWindow() != window",
    "CameraClientCapture.handleMouseButton(",
    "buttonInfo.button(), action, buttonInfo.modifiers()",
    "CameraClientCapture.handleMouseScroll(vertical)",
    "ci.cancel();",
):
    assert token in MIXIN, token

assert MIXIN.count("ci.cancel();") == 2
assert "GuaniaoCameraMouseInputMixin" in CONFIG["client"]
assert "GuaniaoCameraMouseInputMixin" not in CONFIG.get("mixins", [])
for old_token in (
    "CameraClientCapture.handleMouseButton(event.getButton(), event.getAction(), event.getModifiers())",
    "CameraClientCapture.handleMouseScroll(event.getScrollDelta())",
):
    assert old_token in OLD, "Did not preserve original input call: "+old_token
for guard in (
    "SHUTTER_INPUT.release(button);",
    "SHUTTER_INPUT.press(button)",
    "hasShortcutModifiers(modifiers)",
    "shortcutKeysHeld(minecraft)",
    "minecraft.mouseHandler.isMouseGrabbed()",
    "PhotoClientRepository.hasPendingCapture()",
    "beginCleanCapture(viewfinderHand, currentFov())",
    "closeViewfinder();",
    "canUseViewfinderControls()",
):
    assert guard in CAPTURE, "Lost original input protection: "+guard
assert "GuaniaoCameraShutterInputRegression.java" in WORKFLOW
assert "check-camera-mouse-input-26-2.py" in WORKFLOW
print("PASS: Fabric raw camera input routes original button/scroll state and safety")
