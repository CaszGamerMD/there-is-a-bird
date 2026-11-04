#!/usr/bin/env python3
"""Validate original Forge viewfinder rendering is registered on the native HUD."""
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[1]
SRC="src/main/java/EdDYON/guaniao/"
original=subprocess.check_output(["git","show","origin/main:"+SRC+"client/camera/CameraClientEvents.java"],
                                 cwd=ROOT,text=True).replace("\r\n","\n")
client=(ROOT/(SRC+"fabric/GuaniaoFabricClientEntrypoint.java")).read_text("utf-8")
capture=(ROOT/(SRC+"client/camera/CameraClientCapture.java")).read_text("utf-8")
hud=(ROOT/(SRC+"client/camera/CameraViewfinderHud26.java")).read_text("utf-8")
bridge=(ROOT/(SRC+"client/camera/CameraPreviewGuiBridge.java")).read_text("utf-8")
ci=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
gradle=(ROOT/"build.gradle").read_text("utf-8")
assert "RenderGuiEvent.Post" in original and "CameraClientCapture.renderViewfinder(" in original
for needed in (
    "HudElementRegistry.addLast(",
    'Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "camera_viewfinder")',
    "(graphics, deltaTracker) -> CameraClientCapture.renderViewfinder(graphics)",
    "CameraPreviewGuiBridge.clear();",
):
    assert needed in client, needed
assert client.index("CameraClientCapture.disconnect();") < client.index("CameraPreviewGuiBridge.clear();")
assert "public static void renderViewfinder(GuiGraphicsExtractor graphics)" in capture
assert "CameraViewfinderHud26.extract(graphics, currentState," in capture
assert "CameraPreviewGuiBridge.drawPreview(" in capture
assert "if (!viewfinderOpen" in capture and "|| cleanCapturePending" in capture
assert "|| Minecraft.getInstance().gui.screen() != null" in capture
assert "CameraViewfinderOverlay.render(" not in capture
assert "import net.minecraft.client.gui.GuiGraphics;" not in capture
assert "boolean extract(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom)" in hud
assert "if (!lensDrawer.extract(graphics, left, top, right, bottom)) return;" in hud
assert hud.index("if (!lensDrawer.extract(") < hud.index("graphics.fill(0, 0, width, top, BODY)")
assert "return active != null && filter != null" in bridge
assert "GuaniaoCameraHudRegistryProof.java" in gradle
assert "check-camera-hud-registry-26-2.py" in ci
print("PASS: real filtered lens is required before native Fabric camera HUD masks world; logout clears GPU provider")
