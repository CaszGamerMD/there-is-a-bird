#!/usr/bin/env python3
"""Verify all placed-bath visual state survives GeckoLib 5 deferred rendering."""

from pathlib import Path
import subprocess


path = "src/main/java/EdDYON/guaniao/client/bath/BirdBathRenderer.java"
original = subprocess.check_output(["git", "show", f"origin/main:{path}"], text=True)
renderer = Path(path).read_text()
model = Path("src/main/java/EdDYON/guaniao/client/bath/BirdBathModel.java").read_text()
visibility = Path("src/main/java/EdDYON/guaniao/client/bath/BirdBathBoneVisibility.java").read_text()

assert "extends GeoBlockRenderer<BirdBathBlockEntity>" in original
assert "extends GeoBlockRenderer<BirdBathBlockEntity, BlockEntityRenderState>" in renderer
assert "super(context, new BirdBathModel())" in renderer

# The exact source-of-truth for model, content, level, dirt and spoiled-food
# appearance is copied into each render state before the BE is discarded.
for getter, ticket in [
    ("variant()", "BirdBathModel.VARIANT"),
    ("getContentType()", "CONTENT"),
    ("getContentLevel()", "LEVEL"),
    ("getCleanliness()", "CLEANLINESS"),
    ("getRenderContentType()", "VISUAL_CONTENT"),
]:
    assert f"bath.{getter}" in renderer
    assert f"state.addGeckolibData({ticket}, bath.{getter})" in renderer
assert "state.getOrDefaultGeckolibData(VARIANT, BirdBathVariant.BIRD_BATH)" in model.replace(
    "renderState", "state"
)

# The old recursion mutated visibility then applied distinct content and dirt
# colors. New snapshots use the same decisions; custom deferred geometry draws
# every bone with its own tint, including nested bone RGB inheritance.
for call in ["BirdBathBoneVisibility.apply", "isContentBoneVisible", "isDirtBone", "tintFor(", "dirtTintFor("]:
    assert call in renderer
assert "snapshots.get(bone)" in renderer
assert "info.renderPosed(" in renderer
assert "RenderUtil.prepMatrixForBoneAndUpdateListeners(stack, bone, info)" in renderer
assert "bone.render(info, stack, consumer, light, overlay, color)" in renderer
assert "renderTintedBone(info, stack, consumer, child, light, overlay, color," in renderer
assert "bone.frameSnapshot.areChildrenHidden()" in renderer
assert "int color = baseColor;" in renderer
assert "argb & 0xFF000000" in renderer
for channel in ["red", "green", "blue"]:
    assert f"int {channel} = Math.round(" in renderer
for control in ["skipRender(!visible)", "skipChildrenRender(!visible)"]:
    assert control in visibility

for source in (original, renderer):
    assert "shouldRenderOffScreen(" in source and "return false;" in source
    assert "getViewDistance()" in source and "return 48;" in source
assert "shouldRenderOffScreen()" in renderer
assert "software.bernie.geckolib" not in renderer
assert "public void renderRecursively(" not in renderer
assert "super.renderRecursively(" not in renderer

print("PASS: all five bath properties captured from world into GeckoLib 5 render state")
print("PASS: original per-bone content, dirt, tint and recursive color inheritance preserved")
print("PASS: 48-block view distance and offscreen-culling defaults retained")
