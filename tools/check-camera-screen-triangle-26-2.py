#!/usr/bin/env python3
"""Fail if any migrated camera post effect references the removed screenquad."""
import json
from pathlib import Path

root=Path(__file__).resolve().parents[1]
folder=root/"src/main/resources/assets/guaniao/post_effect"
items=sorted(folder.glob("camera_filter_*.json"))
assert len(items)==50
items.append(folder/"camera_optics.json")
for path in items:
    data=json.loads(path.read_text("utf-8"))
    assert len(data["passes"])==2, path
    for stage in data["passes"]:
        assert stage["vertex_shader"]=="minecraft:core/screentriangle", path
        assert "fragment_shader" in stage and "inputs" in stage and "output" in stage, path

optics=(root/"src/main/resources/assets/guaniao/shaders/post/camera_optics.fsh").read_text("utf-8")
assert optics.startswith("#version 330\n#extension GL_ARB_separate_shader_objects : require\n")
assert "layout(location = 0) in vec2 texCoord;" in optics
assert "layout(location = 0) out vec4 fragColor;" in optics
assert "for (int index = 0; index < 32; ++index)" in optics
print("PASS: 51 camera post effects use 26.2 screen triangle and optics varyings")
