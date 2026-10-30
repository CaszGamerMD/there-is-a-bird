#!/usr/bin/env python3
"""Verify original Nikon depth-of-field shader survives 26.2 GLSL UBO migration.

Checks exact shader body preservation, correct input sampler names, 26.2
post-effect topology and uniform-default equivalence. This is NOT runtime
OpenGL shader compilation or proof of dynamic camera settings.
"""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
prefix=ROOT / "src/main/resources/assets/guaniao"
old=(prefix/"shaders/core/camera_optics.fsh").read_text("utf-8")
new=(prefix/"shaders/post/camera_optics.fsh").read_text("utf-8")
schema=json.loads((prefix/"post_effect/camera_optics.json").read_text("utf-8"))
legacy=json.loads((prefix/"shaders/core/camera_optics.json").read_text("utf-8"))
body="// Fixed disk samples avoid the square imprint of a sparse, enlarged 5x5 kernel."
assert body in old and body in new
expected=old[old.index(body):].replace("DiffuseSampler","InSampler").replace(
    "DepthSampler","MainDepthSampler")
assert new[new.index(body):] == expected, "Nikon optics math or 32 disk samples changed"
assert new.startswith("#version 330\n#extension GL_ARB_separate_shader_objects : require\n")
assert "layout(location = 0) in vec2 texCoord;" in new
assert "layout(location = 0) out vec4 fragColor;" in new
assert "uniform sampler2D InSampler;" in new
assert "uniform sampler2D MainDepthSampler;" in new
assert "layout(std140) uniform SamplerInfo {" in new
assert "layout(std140) uniform OpticsConfig {" in new
assert new.count("vec2(") >= 32
assert "for (int index = 0; index < 32; ++index)" in new
assert "linearDepth(" in new and "circleOfConfusion(" in new and "lensUv(" in new
assert "texture(DiffuseSampler" not in new and "texture(DepthSampler" not in new
assert schema["targets"] == {"guaniao_optics_swap": {}}
first, second=schema["passes"]
assert first["vertex_shader"] == "minecraft:core/screentriangle"
assert first["fragment_shader"] == "guaniao:post/camera_optics"
assert first["inputs"] == [
    {"sampler_name":"In","target":"minecraft:main"},
    {"sampler_name":"MainDepth","target":"minecraft:main","use_depth_buffer":True}]
assert first["output"] == "guaniao_optics_swap"
assert second["inputs"] == [{"sampler_name":"In","target":"guaniao_optics_swap"}]
assert second["vertex_shader"] == "minecraft:core/screentriangle"
assert second["fragment_shader"] == "minecraft:post/blit"
assert second["output"] == "minecraft:main"
old_defaults={v["name"]:v["values"][0] for v in legacy["uniforms"] if v["name"]!="OutSize"}
current=first["uniforms"]["OpticsConfig"]
expected_order=["NearPlane","FarPlane","FocusDistance","Aperture","FocalLength","DofMultiplier","LensDistortion"]
assert [v["name"] for v in current] == expected_order
for entry in current:
    assert entry["type"] == "float"
    assert old_defaults[entry["name"]] == entry["value"], entry
assert "ColorModulate" in str(second["uniforms"])
print("PASS: 26.2 optical fragment retains all author lens math and original uniform defaults")
