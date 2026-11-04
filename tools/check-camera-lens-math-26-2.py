#!/usr/bin/env python3
"""Verify camera optics remain author-equivalent after renderer-independent extraction."""
from pathlib import Path
import subprocess
ROOT = Path(__file__).resolve().parents[1]
S = "src/main/java/EdDYON/guaniao/client/camera/"
original = subprocess.check_output(["git", "show", "origin/main:" + S + "CameraClientCapture.java"],
                                   cwd=ROOT,text=True).replace("\r\n", "\n")
capture = (ROOT / (S+"CameraClientCapture.java")).read_text("utf-8")
math = (ROOT / (S+"CameraLensMath.java")).read_text("utf-8")
workflow = (ROOT / ".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
for token in ("FULL_FRAME_SENSOR_WIDTH = 36.0D", "MIN_VIEWFINDER_SENSITIVITY_SCALE = 0.28D",
              "Math.toDegrees(2.0D * Math.atan(FULL_FRAME_SENSOR_WIDTH / (2.0D * focalLength)))",
              "return Math.min(fov, 110.0D);"):
    assert token in original and token in math, token
for token in ("normalized * normalized * (3.0D - 2.0D * normalized)",
              "CameraState.MIN_FOCAL_LENGTH", "CameraState.MAX_FOCAL_LENGTH",
              "storedSensitivity * focalSensitivityScale()",
              "pendingCaptureFov = focalLengthToFov(DEFAULT_FOCAL_LENGTH)"):
    assert token in original or token in math, token
    if token in capture or token in math:
        continue
    raise AssertionError("Lost lens behavior: "+token)
for token in ("return CameraLensMath.focalLengthToFov(focalLength);",
              "CameraLensMath.focalSensitivityScale(currentState.focalLength(),"):
    assert token in capture, token
assert "net.minecraftforge" not in math and "net.minecraft." not in math
assert "GuaniaoCameraLensMathRegression.java" in workflow
assert "javac" in workflow and "CameraLensMath.java" in workflow
print("PASS: original 36 mm FOV, telephoto and sensitivity delegated to testable Java math")
