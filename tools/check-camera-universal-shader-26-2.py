#!/usr/bin/env python3
"""Require exact 50-filter GLSL author parity through the 26.2 backend migration."""
import json
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
legacy="src/main/resources/assets/minecraft/shaders/program/guaniao_camera_universal.fsh"
old=subprocess.check_output(["git","show","origin/main:"+legacy],
                            cwd=ROOT,text=True).replace("\r\n","\n")
new=(ROOT/"src/main/resources/assets/guaniao/shaders/post/camera_universal.fsh").read_text("utf-8")
marker="vec2 safeUV(vec2 uv) {"
assert marker in old and marker in new
assert old[old.index(marker):].replace("DiffuseSampler","InSampler")==new[new.index(marker):], "Effect math changed"
assert new.startswith("#version 330\n#extension GL_ARB_separate_shader_objects : require\n")
for token in ("uniform sampler2D InSampler;", "layout(std140) uniform SamplerInfo",
              "vec2 OutSize;", "layout(std140) uniform FilterConfig",
              "float FilterMode;", "float Time;", "layout(location = 0) in vec2 texCoord;",
              "layout(location = 0) out vec4 fragColor;"):
    assert token in new, token
assert "texture(DiffuseSampler" not in new
assert "vec3 grain(" in new and "Time * 173.0" in new
assert "Time * 24.0" in new and "gl_FragCoord.xy" in new
for n in range(1, 51):
    assert f"FilterMode == {n}.0" in old and f"FilterMode == {n}.0" in new, n
    path=ROOT/f"src/main/resources/assets/guaniao/post_effect/camera_filter_{n:02d}.json"
    first=json.loads(path.read_text("utf-8"))["passes"][0]
    assert first["fragment_shader"]=="guaniao:post/camera_universal"
    assert first["uniforms"]["FilterConfig"]==[
        {"name":"FilterMode","type":"float","value":n},
        {"name":"Time","type":"float","value":0.0}], n
assert "fragColor = vec4(clamp(c, 0.0, 1.0), 1.0);" in new
print("PASS: full original GPU filter formulas, 50 modes, grain, glitch and matching 26.2 uniforms")
