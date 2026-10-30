#!/usr/bin/env python3
"""26.2 optics must interpret reversed-Z camera depth without flipping focus."""
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
shader=(ROOT/"src/main/resources/assets/guaniao/shaders/post/camera_optics.fsh").read_text("utf-8")
legacy=(ROOT/"src/main/resources/assets/guaniao/shaders/core/camera_optics.fsh").read_text("utf-8")
ci=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
for expected in (
    "float z = (1.0 - depth) * 2.0 - 1.0;",
    "circleOfConfusion(sceneDistance, depth <= 0.000001)",
    "circleOfConfusion(linearDepth(sampleDepth), sampleDepth <= 0.000001)",
    "for (int index = 0; index < 32; ++index)",
    "float inverseScene = sky ? 0.0",
):
    assert expected in shader, expected
assert "float z = depth * 2.0 - 1.0;" in legacy
assert "depth >= 0.999999" in legacy
assert "depth >= 0.999999" not in shader
assert "sampleDepth >= 0.999999" not in shader
assert "GuaniaoCameraReversedDepthRegression.java" in ci
assert "check-camera-reversed-depth-26-2.py" in ci
print("PASS: both optical sky samples and near/far reconstruction use 26.2 reversed-Z")
