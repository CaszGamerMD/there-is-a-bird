#!/usr/bin/env python3
"""Verify exact one-to-one authored camera filter JSON IDs on Minecraft 26.2.

Modern post effects are under assets/<namespace>/post_effect/ and use object
targets, explicit vertex/fragment shaders, input samplers, and uniform blocks.
This checks the *schema and 50 source IDs*, not GPU shader compilation.
"""
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
OLD = ROOT / "src/main/resources/assets/guaniao/shaders/post"
NEW = ROOT / "src/main/resources/assets/guaniao/post_effect"
unmigrated = []
for mode in range(1, 51):
    filename = f"camera_filter_{mode:02d}.json"
    before = json.loads((OLD / filename).read_text("utf-8"))
    after = json.loads((NEW / filename).read_text("utf-8"))
    assert before["targets"] == ["guaniao_camera_swap"], filename
    assert len(before["passes"]) == 2, filename
    original, blit = before["passes"]
    assert original["name"] == "guaniao_camera_universal", filename
    assert original["intarget"] == "minecraft:main" and original["outtarget"] == "guaniao_camera_swap", filename
    assert original["uniforms"] == [{"name": "FilterMode", "values": [mode]}], filename
    assert blit["name"] == "blit" and blit["intarget"] == "guaniao_camera_swap" and blit["outtarget"] == "minecraft:main", filename

    assert after["targets"] == {"guaniao_camera_swap": {}}, filename
    assert len(after["passes"]) == 2, filename
    first, second = after["passes"]
    assert first["vertex_shader"] == "minecraft:core/screentriangle", filename
    assert first["fragment_shader"] == "guaniao:post/camera_universal", filename
    assert first["inputs"] == [{"sampler_name": "In", "target": "minecraft:main"}], filename
    assert first["output"] == "guaniao_camera_swap", filename
    assert first["uniforms"] == {"FilterConfig": [
        {"name": "FilterMode", "type": "float", "value": mode},
        {"name": "Time", "type": "float", "value": 0.0}]}, filename
    assert second["vertex_shader"] == "minecraft:core/screentriangle", filename
    assert second["fragment_shader"] == "minecraft:post/blit", filename
    assert second["inputs"] == [{"sampler_name": "In", "target": "guaniao_camera_swap"}], filename
    assert second["output"] == "minecraft:main", filename
    assert second["uniforms"] == {"BlitConfig": [
        {"name": "ColorModulate", "type": "vec4", "value": [1.0, 1.0, 1.0, 1.0]}]}, filename
    assert set(after) == {"targets", "passes"}, filename
    assert all("name" not in p and "intarget" not in p and "outtarget" not in p for p in after["passes"]), filename
    unmigrated.append(filename)

assert len(unmigrated) == 50
assert len(list(NEW.glob("camera_filter_*.json"))) == 50, "Missing or unexpected post-effect resource"
shader = ROOT / "src/main/resources/assets/guaniao/shaders/post/camera_universal.fsh"
assert shader.is_file(), "Original GPU effect shader must be ported"
print("PASS: all 50 original GPU filters have matching 26.2 float uniforms and authored fragment")
