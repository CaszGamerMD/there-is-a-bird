#!/usr/bin/env python3
"""Camera post-chain resource IDs must resolve via 26.2 ShaderManager, not Forge."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
source=(ROOT/"src/main/java/EdDYON/guaniao/client/camera/CameraPostChain26.java").read_text("utf-8")
gradle=(ROOT/"build.gradle").read_text("utf-8")
ci=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
post=ROOT/"src/main/resources/assets/guaniao/post_effect"
for token in (
    'import net.minecraft.client.renderer.PostChain;',
    'import net.minecraft.resources.Identifier;',
    'Set.of(PostChain.MAIN_TARGET_ID)',
    'minecraft.getShaderManager().getPostChain(id, PRIVATE_MAIN_ALIAS)',
    'minecraft.getShaderManager().getPostChain(OPTICS, PRIVATE_MAIN_ALIAS)',
    'Identifier.fromNamespaceAndPath(MOD_ID, "camera_optics")',
    'String.format(Locale.ROOT, "camera_filter_%02d", filter.id())',
    'filter == CameraFilter.NONE',
):
    assert token in source, token
assert "new PostChain(" not in source
assert "net.minecraftforge" not in source
assert "CameraPostChain26.java" in gradle
assert "CameraFilter.java" in gradle and "CameraFilterCategory.java" in gradle
assert "check-camera-postchain-lookup-26-2.py" in ci
assert len(list(post.glob("camera_filter_*.json"))) == 50
for n in range(1, 51):
    p=post/f"camera_filter_{n:02d}.json"
    stage=json.loads(p.read_text("utf-8"))["passes"][0]
    assert stage["fragment_shader"] == "guaniao:post/camera_universal", n
    assert stage["uniforms"]["FilterConfig"][0]["value"] == n, n
assert json.loads((post/"camera_optics.json").read_text("utf-8"))["passes"][0]["fragment_shader"] == "guaniao:post/camera_optics"
print("PASS: original 50 camera shader IDs plus optics map to native 26.2 PostChain manager")
