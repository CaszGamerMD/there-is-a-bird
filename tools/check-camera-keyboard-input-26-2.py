#!/usr/bin/env python3
"""Original Forge camera Escape/screenshot/key shortcut handling on 26.2."""
from pathlib import Path
import json
import subprocess

ROOT=Path(__file__).resolve().parent.parent
mixin=(ROOT/"src/main/java/EdDYON/guaniao/mixin/GuaniaoCameraKeyboardInputMixin.java").read_text("utf-8")
capture=(ROOT/"src/main/java/EdDYON/guaniao/client/camera/CameraClientCapture.java").read_text("utf-8")
config=json.loads((ROOT/"src/main/resources/guaniao.mixins.json").read_text("utf-8"))
workflow=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
gradle=(ROOT/"build.gradle").read_text("utf-8")
old=subprocess.check_output(["git","show",
    "origin/main:src/main/java/EdDYON/guaniao/client/camera/CameraClientEvents.java"],
    cwd=ROOT,text=True).replace("\r\n","\n")

for s in (
    "@Mixin(KeyboardHandler.class)",
    'method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V"',
    'at = @At("HEAD"), require = 1',
    "minecraft.getWindow().getWindow() != window",
    "CameraClientCapture.handleKeyInput(",
    "input.key(), input.scancode(), action, input.modifiers()",
    "GLFW.GLFW_KEY_ESCAPE",
    "screen == null || screen instanceof PauseScreen",
    "CameraClientCapture.closeViewfinder();"
):
    assert s in mixin, s
assert "ci.cancel()" not in mixin and "cancellable = true" not in mixin
assert "GuaniaoCameraKeyboardInputMixin" in config["client"]
assert "GuaniaoCameraKeyboardInputMixin" not in config.get("mixins",[])
assert old.index("CameraClientCapture.handleKeyInput(") < old.index("GLFW.GLFW_KEY_ESCAPE")
for s in ("CameraClientCapture.handleKeyInput(", "CameraClientCapture.closeViewfinder();"):
    assert s in old, s
for s in ("key == GLFW.GLFW_KEY_PRINT_SCREEN",
          "minecraft.options.keyScreenshot.matches(key, scanCode)",
          "hasShortcutModifiers(modifiers)", "isShortcutKey(key)",
          "SHUTTER_INPUT.suspend(true)"):
    assert s in capture, s
assert "minecraft.gui.screen() == null" in capture
assert "GuaniaoCameraKeyboardInputProof.java" in gradle
assert "check-camera-keyboard-input-26-2.py" in workflow
print("PASS: original Escape, screenshot, modifier and no-screen camera input")
