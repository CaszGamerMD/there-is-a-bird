#!/usr/bin/env python3
"""Keep camera scene copy private, depth-aware and backend-agnostic on 26.2."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"src/main/java/EdDYON/guaniao/client/camera"
target=(SRC/"CameraOffscreenTarget26.java").read_text("utf-8")
size=(SRC/"CameraFramebufferSize.java").read_text("utf-8")
workflow=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
gradle=(ROOT/"build.gradle").read_text("utf-8")
for text in (
    'new TextureTarget(', '"guaniao-camera-world-copy"',
    'GpuFormat.RGBA8_UNORM',
    'world.getColorTexture() == null', 'world.getDepthTexture() == null',
    'CameraFramebufferSize.of(world.width, world.height)',
    'RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(',
    'world.getColorTexture(), target.getColorTexture(),',
    'target.copyDepthFrom(world);',
    'target.destroyBuffers();', 'target = null;', 'size = null;',
    'boolean hasMatchingFrame(int width, int height)',
):
    assert text in target, text
for bad in ("GL11", "GL30", "RenderSystem.setShaderTexture(",
            "target.copyColorFrom(world);",
            "world.copyColorFrom(target);", "world.copyDepthFrom(target);",
            "getColorTextureId()", "frameBufferId"):
    assert bad not in target, bad
assert "width <= 0 || height <= 0" in size
assert "CameraOffscreenTarget26.java" in gradle
assert "CameraFramebufferSize.java" in gradle
assert "GuaniaoCameraFramebufferSizeRegression.java" in workflow
assert "check-camera-offscreen-26-2.py" in workflow
print("PASS: 26.2 camera uses private color+depth GPU target, resizes safely, never overwrites world")
