#!/usr/bin/env python3
"""Keep original picker / lens-control inputs while moving to 26.2 Screen events."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
DIR = Path("src/main/java/EdDYON/guaniao/client/camera/")
gradle = (ROOT / "build.gradle").read_text("utf-8")
workflow = (ROOT / ".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
for name in ("CameraFilterPickerScreen.java", "CameraCreativeControlsScreen.java"):
    path = str(DIR / name)
    old = subprocess.check_output(["git", "show", "origin/main:" + path],
                                  cwd=ROOT, text=True).replace("\r\n", "\n")
    new = (ROOT / path).read_text("utf-8")
    for shared in (
        "CameraClientCapture.isViewfinderOpen()",
        "CameraClientCapture.isCleanCapturePending()",
        "CameraClientCapture.currentState()" if "Creative" in name else "CameraClientCapture.currentFilter()",
        "confirmAndClose();", "cancelAndClose();", "GLFW.GLFW_KEY_ESCAPE",
        "GLFW.GLFW_KEY_ENTER", "GLFW.GLFW_KEY_Q", "GLFW.GLFW_KEY_E",
        "CameraKeyMappings.FOCUS.matches(keyCode, scanCode)",
        "CameraClientCapture." + ("commitState(" if "Creative" in name else "commitFilter("),
    ):
        assert shared in old and shared in new, name + ": original behavior changed: " + shared
    for ported in (
        "import net.minecraft.client.input.MouseButtonEvent;",
        "import net.minecraft.client.input.KeyEvent;",
        "minecraft.gui.screen() instanceof",
        "minecraft.gui.setScreen(",
        "public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)",
        "public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double delta)",
        "public boolean keyPressed(KeyEvent event)",
        "double mouseX = event.x();", "double mouseY = event.y();",
        "int button = event.button();", "int keyCode = event.key();",
        "int scanCode = event.scancode();", "super.keyPressed(event)",
    ):
        assert ported in new, name + ": " + ported
    for obsolete in ("minecraft.screen", "minecraft.setScreen(",
                     "Minecraft.getInstance().setScreen(",
                     "super.mouseClicked(mouseX, mouseY, button)",
                     "super.keyPressed(keyCode, scanCode, modifiers)",
                     "public boolean keyPressed(int keyCode,"):
        assert obsolete not in new, name + ": obsolete " + obsolete
    if "Creative" in name:
        for token in (
            "public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY)",
            "super.mouseDragged(event, dragX, dragY)",
            "mouseScrolled(0.0D, 0.0D, 0.0D, 1.0D)",
            "mouseScrolled(0.0D, 0.0D, 0.0D, -1.0D)",
            "CameraClientCapture.previewState(",
            "CameraClientPreferences.setShowGuides(",
            "CameraClientPreferences.setDepthOfFieldEnabled(",
        ):
            assert token in new, token
    else:
        for token in ("selectOriginal();", "select(filters.get(filterIndex));",
                      "moveSelection(delta > 0.0D ? -1 : 1)", "randomFilter();"):
            assert token in new, token
assert "GuaniaoCameraScreenInputProof.java" in gradle
assert "check-camera-screen-input-26-2.py" in workflow
print("PASS: both original camera control GUIs use 26.2 mouse/keyboard records with unchanged decisions")
