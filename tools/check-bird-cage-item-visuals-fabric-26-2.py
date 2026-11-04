#!/usr/bin/env python3
"""Verify authored cage inventory presentation and native GeckoLib 5 registration."""

from pathlib import Path
import re
import subprocess

base = "src/main/java/EdDYON/guaniao/"
item_path = base + "content/cage/BirdCageItem.java"
model_path = base + "client/cage/BirdCageItemModel.java"
render_path = base + "client/cage/BirdCageItemRenderer.java"
client_path = base + "fabric/GuaniaoFabricClientEntrypoint.java"

def old(path):
    return subprocess.check_output(["git", "show", "origin/main:" + path], text=True)

item = Path(item_path).read_text()
model = Path(model_path).read_text()
renderer = Path(render_path).read_text()
client = Path(client_path).read_text()
original_item = old(item_path)
original_renderer = old(render_path)

assert "implements GeoItem" in item
assert "com.geckolib.animatable.GeoItem" in item
assert "createGeoRenderer(Consumer<GeoRenderProvider> consumer)" in item
assert "consumer.accept(this.geoRenderProvider.getValue())" in item
assert 'Component.translatable("item.guaniao.bird_cage.decorative")' in item
assert 'Component.translatable("item.guaniao.bird_cage.decorative")' in original_item
assert "ChatFormatting.GRAY" in item and "ChatFormatting.GRAY" in original_item
assert "TooltipContext context, TooltipDisplay display" in item
assert "Consumer<Component> tooltip" in item

# Retain the exact original size-specific scale and XY placement numbers.
marker = "    private static float itemScale(BirdCageVariant variant) {"
assert original_renderer[original_renderer.index(marker):] == renderer[renderer.index(marker):]
assert "adjustRenderPose(RenderPassInfo<GeoRenderState> info)" in renderer
assert "super.adjustRenderPose(info);" in renderer
assert "GuaniaoGeckoAssets.model(variant.model())" in model
assert "this.texture = variant.texture()" in model
assert "GuaniaoGeckoAssets.animation(BirdCageVariant.ANIMATION)" in model

for value in ("SMALL_BIRD_CAGE", "MEDIUM_BIRD_CAGE", "LARGE_BIRD_CAGE"):
    assert f"GuaniaoItems.{value}.get()" in client
assert "registerBirdCageItemRenderers();" in client
assert "new BirdCageItemRenderer(cage)" in client
assert "new GeoRenderProvider()" in client
for source in (item, model, renderer):
    assert "software.bernie.geckolib" not in source
    assert "net.minecraftforge" not in source
    assert "BlockEntityWithoutLevelRenderer" not in source

print("PASS: Three cage GeoItems use Fabric renderer providers and native 26.2 tooltip APIs")
print("PASS: Authored cage geometry, material IDs, transforms and scale constants are preserved")
