#!/usr/bin/env python3
"""Camera framebuffer copies must occur only in Fabric's world draw phase."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
prefix = ROOT / "src/main/java/EdDYON/guaniao"
hook=(prefix/"client/camera/CameraWorldFrame26.java").read_text("utf-8")
capture=(prefix/"client/camera/CameraClientCapture.java").read_text("utf-8")
offscreen=(prefix/"client/camera/CameraOffscreenTarget26.java").read_text("utf-8")
bridge=(prefix/"client/camera/CameraPreviewGuiBridge.java").read_text("utf-8")
entry=(prefix/"fabric/GuaniaoFabricClientEntrypoint.java").read_text("utf-8")
gradle=(ROOT/"build.gradle").read_text("utf-8")
ci=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
for required in (
    "LevelRenderEvents.END_MAIN.register(context -> captureWorld());",
    "private static final CameraOffscreenTarget26 OFFSCREEN",
    "CameraClientCapture.isViewfinderOpen()",
    "CameraClientCapture.isCleanCapturePending()",
    "!CameraPreviewGuiBridge.hasRenderer()",
    "OFFSCREEN.copyWorld(source)",
    "copiedWorld = updated;",
    "OFFSCREEN.close();",
):
    assert required in hook, required
assert "CameraWorldFrame26.register();" in entry
assert "CameraWorldFrame26.reset();" in entry
assert capture.count("CameraWorldFrame26.reset();") == 2
assert "private static volatile Renderer renderer;" in bridge
assert "public static boolean hasRenderer()" in bridge
assert hook.count("CameraOffscreenTarget26") >= 2
for forbidden in (
    "GL11", "GL30", "RenderSystem.gl", "CameraPreviewGuiBridge.install(",
    "Screenshot.takeScreenshot(", "minecraft.gameRenderer.mainRenderTarget().copyColorFrom(",
):
    assert forbidden not in hook, forbidden
assert "GuaniaoCameraLevelRenderProof.java" in gradle
assert "check-camera-world-frame-26-2.py" in ci
for required in (
    "import com.mojang.blaze3d.GpuFormat;",
    "RenderSystem.assertOnRenderThread();",
    "target.getColorTexture() == null || target.getDepthTexture() == null",
    "RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(",
    "world.getColorTexture(), target.getColorTexture(),",
    "0, 0, 0, 0, 0, next.width(), next.height());",
    "target.copyDepthFrom(world);",
):
    assert required in offscreen, required
assert "target.copyColorFrom(world)" not in offscreen
print("PASS: native draw-phase camera copy is gated, private and cleaned on shutter/logout")
