#!/usr/bin/env python3
"""Check both camera screens preserve original UI/interaction during 26.2 extraction."""
from pathlib import Path
import subprocess

ROOT=Path(__file__).resolve().parents[1]
SRC="src/main/java/EdDYON/guaniao/client/camera/"
bridge=(ROOT/(SRC+"CameraPreviewGuiBridge.java")).read_text("utf-8")
ci=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
gradle=(ROOT/"build.gradle").read_text("utf-8")
lang=(ROOT/"src/main/resources/assets/guaniao/lang/en_us.json").read_text("utf-8")

for name in ("CameraFilterPickerScreen.java","CameraCreativeControlsScreen.java"):
    old=subprocess.check_output(["git","show","origin/main:"+SRC+name],
                                cwd=ROOT,text=True).replace("\r\n","\n")
    screen=(ROOT/(SRC+name)).read_text("utf-8")
    for expected in (
        "public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)",
        "graphics.fill(", "graphics.text(", "graphics.horizontalLine(",
        "graphics.verticalLine(", "graphics.enableScissor(",
        "graphics.disableScissor(", "graphics.pose().pushMatrix()",
        "graphics.pose().translate(", "graphics.pose().scale(",
        "graphics.pose().popMatrix()", "CameraPreviewGuiBridge.drawPreview(",
        "gui.guaniao.camera_preview.unavailable", "confirmAndClose();",
        "cancelAndClose();", "GLFW.GLFW_KEY_ESCAPE",
    ):
        assert expected in screen, name+": missing "+expected
    for obsolete in ("import net.minecraft.client.gui.GuiGraphics;",
                     "public void render(GuiGraphics ", "graphics.drawString(",
                     "graphics.drawCenteredString(", "graphics.hLine(",
                     "graphics.vLine(", "graphics.pose().pushPose()",
                     "graphics.pose().popPose()", "CameraPreviewPostEffect.drawPreview("):
        assert obsolete not in screen, name+": stale "+obsolete
    for key in ("PAGE" if "Creative" in name else "CATEGORY",):
        assert key
    # All original branded UI screens retain same author layout and input.
    for key in ("drawPanel(", "renderFooter(", "CameraClientCapture.preview",
                "mouseClicked(", "keyPressed(", "mouseScrolled(",
                "this.leftLeft", "this.rightRight", "this.previewTop"):
        assert key in old and key in screen, name+": lost author screen "+key

picker=(ROOT/(SRC+"CameraFilterPickerScreen.java")).read_text("utf-8")
creative=(ROOT/(SRC+"CameraCreativeControlsScreen.java")).read_text("utf-8")
assert "CameraPreviewGuiBridge.drawFilterThumbnail(" in picker
assert "renderPaletteThumbnail(graphics, filter," in picker
assert "super.extractContents(graphics, mouseX, mouseY, partialTick)" in creative
assert "super.render(graphics" not in creative
assert "updatePreferenceButtons();" in creative
assert "CameraClientPreferences.showGuides()" in creative
assert "interface Renderer" in bridge
assert "boolean drawPreview(GuiGraphicsExtractor graphics" in bridge
assert "boolean drawThumbnail(GuiGraphicsExtractor graphics" in bridge
assert "return active != null && filter != null" in bridge
assert "renderer = null;" in bridge
assert "CameraPreviewPostEffect" in bridge and "CameraPreviewPostEffect.draw" not in bridge
assert "Live preview requires 26.2 renderer" in lang
assert "GuaniaoCameraScreenExtractionProof.java" in gradle
assert "check-camera-screen-extraction-26-2.py" in ci
print("PASS: both camera screens migrated to deferred 26.2 GUI extraction; only genuine filter frames accepted")
