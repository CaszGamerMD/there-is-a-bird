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
    assert first["vertex_shader"] == "minecraft:core/screenquad", filename
    assert first["fragment_shader"] == "guaniao:post/camera_universal", filename
    assert first["inputs"] == [{"sampler_name": "In", "target": "minecraft:main"}], filename
    assert first["output"] == "guaniao_camera_swap", filename
    assert first["uniforms"] == {"FilterConfig": [{"name": "FilterMode", "type": "int", "value": mode}]}, filename
    assert second["vertex_shader"] == "minecraft:core/screenquad", filename
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
# The fragment shader is explicitly a separate blocker. Never substitute
# a no-op fragment while claiming parity with the original 50 filters.
shader = ROOT / "src/main/resources/assets/guaniao/shaders/post/camera_universal.fsh"
if not shader.is_file():
    print("BLOCKER: original universal filter GPU shader not ported; JSON definitions staged only")
print("PASS: all 50 26.2 post effect resources preserve original two-pass ID and blit wiring")
