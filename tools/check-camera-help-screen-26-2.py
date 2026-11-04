#!/usr/bin/env python3
"""Verify camera first-use help card migrated to the 26.2 extractor."""
from pathlib import Path
import subprocess
ROOT = Path(__file__).resolve().parents[1]
PATH = "src/main/java/EdDYON/guaniao/client/camera/CameraHelpScreen.java"
original = subprocess.check_output(["git", "show", "origin/main:" + PATH],
                                  cwd=ROOT, text=True).replace("\r\n", "\n")
ported = (ROOT / PATH).read_text("utf-8").replace("\r\n", "\n")
for token in (
    "Math.min(240, Math.max(1, this.width - 24))",
    "Math.min(128, Math.max(1, this.height - 16))",
    "this.top + this.cardHeight - 26",
    'Component.translatable("gui.guaniao.camera_help.resume_short")',
    "compactControlLines()", "controlLines().get(hoveredControl)",
    "CameraKeyMappings.OPEN_CREATIVE_CONTROLS.getTranslatedKeyMessage()",
    "CameraKeyMappings.OPEN_FILTER_LIBRARY.getTranslatedKeyMessage()",
    "CameraKeyMappings.FOCUS.getTranslatedKeyMessage()",
    "CameraClientPreferences.markHelpSeen();",
    "CameraClientCapture.openViewfinder(this.hand);",
    "this.owner.getItemInHand(this.hand) == this.camera",
    "CameraClientCapture.isViewfinderOpen()",
    "this.font.plainSubstrByWidth(label, Math.max(1, availableWidth - this.font.width(\"…\")))"
):
    assert token in original and token in ported, token
for token in (
    "public void extractContents(GuiGraphicsExtractor graphics",
    "graphics.horizontalLine(", "graphics.verticalLine(",
    "graphics.text(this.font, this.title",
    "graphics.text(this.font, footer.get(i)",
    "super.extractContents(graphics, mouseX, mouseY, partialTick);",
    "graphics.setTooltipForNextFrame(this.font, controlLines().get(hoveredControl)",
    "minecraft.gui.setScreen(", "minecraft.gui.screen()"
):
    assert token in ported, token
for deprecated in ("import net.minecraft.client.gui.GuiGraphics;",
                   "public void render(GuiGraphics", "graphics.drawString(",
                   "graphics.renderTooltip(", "graphics.hLine(", "graphics.vLine(",
                   "minecraft.setScreen(", "Minecraft.getInstance().setScreen("):
    assert deprecated not in ported, deprecated
assert "GuaniaoCameraHelpExtractionProof.java" in (ROOT / "build.gradle").read_text("utf-8")
assert "check-camera-help-screen-26-2.py" in (ROOT / ".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
print("PASS camera first-use help: original panel, control labels, owner guards, GUI extraction")
