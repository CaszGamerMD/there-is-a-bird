#!/usr/bin/env python3
"""Validate asynchronous 26.2 screenshot readback and original camera color encoding."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CAM = (ROOT / "src/main/java/EdDYON/guaniao/client/camera/CameraClientCapture.java").read_text("utf-8")
SAM = (ROOT / "src/main/java/EdDYON/guaniao/client/camera/CameraPhotoSampler.java").read_text("utf-8")
PRE = (ROOT / "src/main/java/EdDYON/guaniao/client/camera/CameraPreviewPostEffect.java").read_text("utf-8")
CI = (ROOT / ".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
for token in (
    "UUID uploadId = pendingCaptureId;",
    "CameraFilter filter = pendingCaptureState.filter();",
    "int guiWidth = minecraft.getWindow().getGuiScaledWidth();",
    "int guiHeight = minecraft.getWindow().getGuiScaledHeight();",
    "int fontHeight = minecraft.font.lineHeight;",
    "minecraft.gameRenderer.mainRenderTarget()",
    "Screenshot.takeScreenshot(captureTarget, image -> {",
    "try (image)",
    "CameraPhotoSampler.cropSquare(",
    "CameraPhotoSampler.argbToAbgr(image.getPixel(x, y))",
    "CameraImageFilters.apply(pixels, photoSize, photoSize, filter, System.nanoTime());",
    "PhotoImageCodec.encodeJpeg(pixels, photoSize, photoSize)",
    "minecraft.execute(() -> {",
    "PhotoClientRepository.upload(uploadId, hand, jpeg);",
    "PhotoClientRepository.cancelCapture(uploadId);",
    "captureSubmitted = false;",
):
    assert token in CAM, token
assert CAM.index("captureSubmitted = true;") < CAM.index("Screenshot.takeScreenshot(captureTarget")
assert "Screenshot.takeScreenshot(captureTarget)" not in CAM
assert "image.getPixelRGBA(" not in CAM
assert "getMainRenderTarget()" not in CAM and "getMainRenderTarget()" not in PRE
assert "minecraft.gameRenderer.mainRenderTarget()" in PRE
assert "static int argbToAbgr(int argb)" in SAM
assert "0xFF00FF00" in SAM and "0x00FF0000" in SAM
assert "GuaniaoCameraPixelColorRegression.java" in CI
assert "check-camera-screenshot-26-2.py" in CI
print("PASS: callback-based screenshot, snapshot state, native-image close, ABGR JPEG and async failure cleanup")
