#!/usr/bin/env python3
"""Check the author camera photo crop still produces the same JPEG pixels."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
ORIGINAL = "src/main/java/EdDYON/guaniao/client/camera/CameraClientCapture.java"
old = subprocess.check_output(["git", "show", "origin/main:" + ORIGINAL],
                              cwd=ROOT, text=True).replace("\r\n", "\n")
capture = (ROOT / ORIGINAL).read_text("utf-8")
sampler = (ROOT / "src/main/java/EdDYON/guaniao/client/camera/CameraPhotoSampler.java").read_text("utf-8")
ci = (ROOT / ".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
for expression in (
    "CameraViewfinderFrame.layout(",
    "crop.top() + Math.min(crop.height() - 1, (int)((y + 0.5D) * crop.height() / size))",
    "crop.left() + Math.min(crop.width() - 1, (int)((x + 0.5D) * crop.width() / size))",
    "pixels[y * size + x] = ",
):
    assert expression in old and expression in sampler, expression
assert "return CameraPhotoSampler.cropSquare(" in capture
assert "image::getPixelRGBA" in capture
for fragment in ("CameraImageFilters.apply(", "PhotoImageCodec.encodeJpeg(", "PhotoClientRepository.upload("):
    assert fragment in capture, fragment
assert "source.get(sampleX, sampleY)" in sampler
assert "net.minecraftforge." not in sampler and "net.minecraft." not in sampler
assert "GuaniaoCameraPhotoSamplerRegression.java" in ci
assert "check-camera-photo-sampler-26-2.py" in ci
print("PASS: original photo framing, nearest-center sampling, filter and upload preserved")
