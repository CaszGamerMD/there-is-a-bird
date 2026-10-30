#!/usr/bin/env python3
"""Guard original photograph viewer layout, full-image UVs and export on 26.2."""
from pathlib import Path
import subprocess
ROOT = Path(__file__).resolve().parent.parent
SCREEN = "src/main/java/EdDYON/guaniao/client/camera/PhotographScreen.java"
ACTIONS = "src/main/java/EdDYON/guaniao/client/camera/PhotographClientActions.java"
old = subprocess.check_output(["git", "show", "origin/main:" + SCREEN],
                              cwd=ROOT, text=True).replace("\r\n", "\n")
new = (ROOT / SCREEN).read_text("utf-8").replace("\r\n", "\n")
action = (ROOT / ACTIONS).read_text("utf-8")
for s in (
    "PhotographTextureCache.preload(this.photograph)",
    "Math.min(PhotographData.IMAGE_SIZE,",
    "this.imageX = (this.width - this.imageSize) / 2",
    "this.captionY = this.imageY + this.imageSize + 10",
    "this.buttonY = this.captionY + this.font.lineHeight + 9",
    'Component.translatable("gui.guaniao.photograph.export")',
    'Component.translatable("gui.guaniao.photograph.close")',
    "PhotographData.photographer(this.photograph)",
    "PhotoAuthorText.component(photographer)",
    "PhotographTextureCache.export(this.photograph)",
    "this.font.substrByWidth(text, Math.max(0, this.imageSize - this.font.width(suffix)))",
    "isPauseScreen()"):
    assert s in old and s in new, "Original photo behavior changed: " + s
for s in ("public void extractContents(GuiGraphicsExtractor graphics",
          "super.extractContents(graphics, mouseX, mouseY, partialTick)",
          "graphics.fill(x, y, x + size, y + size, 0xFF2A2A2A)",
          "graphics.text(this.font, Language.getInstance().getVisualOrder(text)",
          "PhotographTextureCache.readyTextureFor(this.photograph)",
          "graphics.blit(RenderPipelines.GUI_TEXTURED, texture,",
          "imageSize, imageSize, imageSize, imageSize)"):
    assert s in new, s
for legacy in ("GuiGraphics graphics", "ResourceLocation",
               "graphics.pose().pushPose()", "graphics.drawString(",
               "graphics.blit(texture,", "this.renderBackground(graphics)"):
    assert legacy not in new, legacy
assert "Minecraft.getInstance().gui.setScreen(new PhotographScreen(stack));" in action
assert "GuaniaoPhotographViewerProof.java" in (ROOT / "build.gradle").read_text("utf-8")
assert "check-photograph-viewer-26-2.py" in (ROOT / ".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
print("PHOTOGRAPH VIEWER 26.2 SOURCE PARITY OK: full UV, caption, export and close")
