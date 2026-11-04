#!/usr/bin/env python3
"""Check authored six-variant item models, GUI transforms and inventory bone hiding."""

from pathlib import Path
import re


root = Path("src/main/java/EdDYON/guaniao/client/bath")
renderer = (root / "BirdBathItemRenderer.java").read_text()
model = (root / "BirdBathItemModel.java").read_text()
visibility = (root / "BirdBathBoneVisibility.java").read_text()
variant = Path("src/main/java/EdDYON/guaniao/content/bath/BirdBathVariant.java").read_text()
build = Path("build.gradle").read_text()
client = Path("src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricClientEntrypoint.java").read_text()

authored_content = re.search(r"CONTENT_BONES = Set\.of\((.*?)\);", visibility, re.S).group(1)
authored_dirt = re.search(r"DIRT_BONES = Set\.of\((.*?)\);", visibility, re.S).group(1)
item_hidden = re.search(r"HIDDEN_BONES = Set\.of\((.*?)\);", renderer, re.S).group(1)
bones = lambda source: set(re.findall(r'"([a-z_]+)"', source))
assert bones(item_hidden) == bones(authored_content) | bones(authored_dirt), "Inventory item content/dirt visibility changed"

assert "new BirdBathItemModel(item.variant())" in renderer
assert "new BirdBathItemRenderer(bath)" in client
assert 'normalizedId(variant.model(), ".geo.json")' in model
assert 'normalizedId(variant.animation(), ".animation.json")' in model
assert "this.texture = variant.texture();" in model
assert "String path = authored.getPath();" in model
assert "path.substring(0, path.length() - suffix.length())" in model
assert "geckolib/models" in build and "geckolib/animations" in build
assert variant.count('"geo/') == 6 and variant.count('"textures/block/') == 6

for transform in (
    "poseStack.translate(0.5F, 0.5F, 0.5F)",
    "poseStack.mulPose(Axis.XP.rotationDegrees(90.0F))",
    "poseStack.scale(0.8F, 0.8F, 0.8F)",
    "poseStack.translate(0.0F, -1.4F, 0.0F)",
    "return 0.28F;", "return -0.12F;", "return 0.0F;", "return 0.5F;",
):
    assert transform in renderer, "Authored GUI or held-item presentation changed: " + transform
assert "super.adjustRenderPose(info);" in renderer
assert "bone.skipRender(true)" in renderer and "bone.skipChildrenRender(true)" in renderer
assert "renderRecursively(" not in renderer and "software.bernie.geckolib" not in renderer + model

print("PASS: all six bath assets retain their authored model, texture and animation paths")
print("PASS: inventory model hides exactly the original 23 content and dirt bones")
print("PASS: slot top view, small-bird rim clearance and held-item scale remain authored")
