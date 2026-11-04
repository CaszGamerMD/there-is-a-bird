#!/usr/bin/env python3
"""Verify 26.2 optics uniforms match the seven original shader inputs."""
import json
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
path="src/main/java/EdDYON/guaniao/client/camera/CameraOpticsShader.java"
old=subprocess.check_output(["git","show","origin/main:"+path],cwd=ROOT,text=True).replace("\r\n","\n")
new=(ROOT/path).read_text("utf-8")
params=(ROOT/"src/main/java/EdDYON/guaniao/client/camera/CameraOpticsParameters.java").read_text("utf-8")
effect=json.loads((ROOT/"src/main/resources/assets/guaniao/post_effect/camera_optics.json").read_text("utf-8"))
shader=(ROOT/"src/main/resources/assets/guaniao/shaders/post/camera_optics.fsh").read_text("utf-8")
ci=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
names=("NearPlane","FarPlane","FocusDistance","Aperture","FocalLength","DofMultiplier","LensDistortion")
for name in names:
    assert f'setUniform("{name}",' in old, "Legacy missing "+name
    assert f'setUniform("{name}", parameters.' in new, "New bridge missing "+name
    assert name in shader, "Shader missing "+name
    assert name in params or name[0].lower()+name[1:] in params, "Param missing "+name
assert "NEAR_PLANE = 0.05F" in params and "NEAR_PLANE = 0.05F" in old
for field in ("state.focusDistance()", "state.aperture().fStop()", "state.focalLength()",
              "state.lens().depthOfFieldMultiplier()", "state.lens().distortion()"):
    assert field in params and field in old, "Formula changed "+field
assert "depthOfFieldEnabled ? state.lens().depthOfFieldMultiplier() : 0.0F" in params
assert "CameraOpticsParameters.fromCamera(" in new
assert "if (!parameters.needsOpticsPass())" in new
assert "net.minecraftforge" not in params and "net.minecraft" not in params
assert [entry["name"] for entry in effect["passes"][0]["uniforms"]["OpticsConfig"]] == list(names)
assert "GuaniaoCameraOpticsParametersRegression.java" in ci
print("PASS: all original dynamic optics state maps to 26.2 uniform group, unchanged bypass")
