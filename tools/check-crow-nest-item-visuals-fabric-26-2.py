#!/usr/bin/env python3
"""Ensure the item nest renders empty without changing the authored nest assets."""

from pathlib import Path
import subprocess

base = "src/main/java/EdDYON/guaniao/"
item_path = base + "content/nest/CrowNestItem.java"
renderer_path = base + "client/nest/CrowNestItemRenderer.java"
model_path = base + "client/nest/CrowNestItemModel.java"
client_path = base + "fabric/GuaniaoFabricClientEntrypoint.java"

def original(path):
    return subprocess.check_output(["git", "show", "origin/main:" + path], text=True)

item = Path(item_path).read_text()
renderer = Path(renderer_path).read_text()
model = Path(model_path).read_text()
client = Path(client_path).read_text()
old_renderer = original(renderer_path)

assert "implements GeoItem" in item
assert "createGeoRenderer(Consumer<GeoRenderProvider> consumer)" in item
assert "consumer.accept(this.geoRenderProvider.getValue())" in item
assert "registerCrowNestItemRenderer();" in client
assert "GuaniaoItems.CROW_NEST.get()" in client
assert "CrowNestItemRenderer::new" in client
assert "new GeoRenderProvider()" in client
assert "CrowNestItemRenderer extends GeoItemRenderer<CrowNestItem>" in renderer
assert "adjustRenderPose(RenderPassInfo<GeoRenderState> info)" in renderer
for v in ("0.25F", "-0.18F", "0.62F"):
    assert v in old_renderer and v in renderer
assert "super.adjustRenderPose(info);" in renderer
assert 'Set.of("egg1", "egg2", "egg3")' in renderer
assert "CrowNestRenderer.applyEggVisibility(bone, 0)" in old_renderer
assert "bone.skipRender(true)" in renderer
assert "bone.skipChildrenRender(true)" in renderer
for original_asset in ("geo/crow_nest.geo.json", "textures/block/crow_nest.png",
                       "animations/crow_nest.animation.json"):
    assert original_asset in model
assert "GuaniaoGeckoAssets.model(MODEL)" in model
assert "GuaniaoGeckoAssets.animation(ANIMATION)" in model
for source in (item, renderer):
    assert "net.minecraftforge" not in source
    assert "software.bernie.geckolib" not in source
    assert "BlockEntityWithoutLevelRenderer" not in source

print("PASS: Inventory crow nest preserves original model, pose and empty-egg appearance")
print("PASS: Client-only GeckoLib 5 provider installed with native 26.2 render state")
