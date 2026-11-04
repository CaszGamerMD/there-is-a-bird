#!/usr/bin/env python3
"""Source parity for original viewfinder bezel and native 26.2 rendering."""
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parent.parent
P="src/main/java/EdDYON/guaniao/client/camera/"
old=subprocess.check_output(["git","show","origin/main:"+P+"CameraViewfinderOverlay.java"],cwd=ROOT,text=True).replace("\r\n","\n")
new=(ROOT/(P+"CameraViewfinderHud26.java")).read_text("utf-8")
for token in (
    "BODY = 0xFF181817","EDGE = 0xFF363632","INK = 0xFFEBE8D8",
    "MUTED = 0xFFACA99C","AMBER = 0xFFE3BC73",
    "CameraViewfinderFrame.layout(width, height, minecraft.font.lineHeight)",
    "graphics.fill(0, 0, width, top, BODY)","graphics.fill(0, bottom, width, height, BODY)",
    "graphics.fill(0, top, left, bottom, BODY)","graphics.fill(right, top, width, bottom, BODY)",
    "drawBezel(graphics, left, top, right, bottom)",
    "drawFocusMark(graphics, (left + right) / 2, (top + bottom) / 2)",
    "drawParameters(graphics, minecraft.font, state, left, top, right, bottom)",
    "if (CameraClientPreferences.showGuides())","drawGuides(graphics, left, top, right, bottom)",
    "int radius = 7","int color = 0xBBEFEBDD",
    'Math.round(state.focalLength()) + " mm"',
    'state.hasInfiniteFocus() ? "∞"',
    'String.format(Locale.ROOT, "%.1f m", state.focusDistance())',
    "int split = size * 2 / 3","for (int x = 1; x <= 2; x++)",
    "for (int y = 1; y <= 2; y++)","0x55EBE8D8"):
    assert token in old and token in new, token
for token in ("interface LensDrawer","lensDrawer.extract(graphics, left, top, right, bottom)",
              "java.util.Objects.requireNonNull(lensDrawer",
              "graphics.pose().pushMatrix()","graphics.pose().popMatrix()",
              "graphics.pose().scale(scale, scale)","graphics.text(font, value, 0, 0, color, false)"):
    assert token in new, token
for bad in ("net.minecraftforge.","CameraPreviewPostEffect","import net.minecraft.client.gui.GuiGraphics;",
            "graphics.pose().pushPose();","graphics.drawString("):
    assert bad not in new, bad
assert "HudElementRegistry" not in new, "Do not register before the real filtered lens"
gradle=(ROOT/"build.gradle").read_text("utf-8")
workflow=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
assert "GuaniaoCameraViewfinderHudProof.java" in gradle
assert "check-camera-viewfinder-hud-26-2.py" in workflow
assert "GuaniaoViewfinderFrameRegression.java" in workflow
print("PASS: authored 26.2 viewfinder bezel, guides, and required real lens")
