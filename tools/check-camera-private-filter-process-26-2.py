#!/usr/bin/env python3
"""Author filters must execute only on the private 26.2 camera GPU target."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"src/main/java/EdDYON/guaniao/client/camera"
post=(src/"CameraPostChain26.java").read_text("utf-8")
world=(src/"CameraWorldFrame26.java").read_text("utf-8")
target=(src/"CameraOffscreenTarget26.java").read_text("utf-8")
workflow=(root/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
gradle=(root/"build.gradle").read_text("utf-8")
for token in (
    "public static boolean processFilterOnPrivate(",
    "if (filter == CameraFilter.NONE) return true;",
    "if (chain == null) return false;",
    "chain.process(privateTarget, GraphicsResourceAllocator.UNPOOLED);",
    "return true;",
):
    assert token in post, token
for token in (
    "RenderTarget updated = OFFSCREEN.copyWorld(source);",
    "CameraFilter filter = CameraClientCapture.currentFilter();",
    "CameraPostChain26.processFilterOnPrivate(minecraft, filter, updated)",
    "if (updated == null",
    "copiedWorld = null;",
    "copiedWorld = updated;",
    "!CameraPreviewGuiBridge.hasRenderer()",
):
    assert token in world, token
assert world.index("OFFSCREEN.copyWorld(source)") < world.index("processFilterOnPrivate(")
assert world.index("processFilterOnPrivate(") < world.index("copiedWorld = updated;")
assert "minecraft.gameRenderer.mainRenderTarget()" in world
assert "target.copyColorFrom(world);" in target
assert "target.copyDepthFrom(world);" in target
for forbidden in (
    "chain.process(source,", "chain.process(minecraft.gameRenderer.mainRenderTarget()",
    "GraphicsResourceAllocator.UNPOOLED" # allocator belongs ONLY in post-chain
):
    assert forbidden not in world, forbidden
assert "net.minecraftforge" not in post
assert "GuaniaoCameraPostProcessProof.java" in gradle
assert "check-camera-private-filter-process-26-2.py" in workflow
print("PASS: complete author GPU filter is executed only on a private camera framebuffer")
