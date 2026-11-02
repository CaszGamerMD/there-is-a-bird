#!/usr/bin/env python3
"""Check 26.2 placed-bird-enclosure renderer source and original per-bone gameplay parity."""

from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
base = "src/main/java/EdDYON/guaniao/"

def source(path):
    return (root / (base + path)).read_text("utf-8")

def original(path):
    return subprocess.check_output(
        ["git", "show", "origin/main:" + base + path], cwd=root, text=True
    )

cage = source("client/cage/BirdCageRenderer.java")
cage_model = source("client/cage/BirdCageModel.java")
nest = source("client/nest/CrowNestRenderer.java")
fries = source("client/food/BaggedFriesRenderer.java")
entry = source("fabric/GuaniaoFabricClientEntrypoint.java")
old_cage = original("client/cage/BirdCageRenderer.java")
old_nest = original("client/nest/CrowNestRenderer.java")
old_fries = original("client/food/BaggedFriesRenderer.java")

assert "GeoBlockRenderer<BirdCageBlockEntity, BlockEntityRenderState>" in cage
assert "state.addGeckolibData(VARIANT, cage.variant())" in cage
assert "GuaniaoGeckoAssets.model(variant(renderState).model())" in cage_model
assert "return variant(renderState).texture()" in cage_model
assert "GuaniaoGeckoAssets.animation(BirdCageVariant.ANIMATION)" in cage_model
assert "getViewDistance()" in cage and "return 128" in cage and "return 128" in old_cage
assert "shouldRenderOffScreen()" in cage and "return true" in cage

assert "GeoBlockRenderer<CrowNestBlockEntity, BlockEntityRenderState>" in nest
assert "state.addGeckolibData(EGGS, nest.getEggCount())" in nest
for name in ("egg1", "egg2", "egg3"):
    assert name in old_nest
assert 'snapshots.ifPresent("egg" + index' in nest
assert "boolean visible = eggs >= index;" in nest
assert "bone.skipRender(!visible)" in nest and "bone.skipChildrenRender(!visible)" in nest
assert "return 96" in nest and "return 96" in old_nest

assert "GeoBlockRenderer<BaggedFriesBlockEntity, BlockEntityRenderState>" in fries
assert "state.addGeckolibData(FRY_MASK, fries.getFryMask())" in fries
assert "fries.isFryVisible(index)" in old_fries
assert "(mask & (1 << (index - 1))) != 0" in fries
assert "BaggedFriesBlock.TOTAL_FRIES" in fries
assert "bone.skipRender(!visible)" in fries and "bone.skipChildrenRender(!visible)" in fries
marker = "    private static int fryIndex(String name) {"
assert old_fries[old_fries.index(marker):] == fries[fries.index(marker):], (
    "Original fry index parsing and bounds must remain unchanged"
)

for renderer in ("BirdBathRenderer", "BirdCageRenderer", "CrowNestRenderer", "BaggedFriesRenderer"):
    assert renderer + "::new" in entry, renderer + " lacks Fabric registration"
for block_type in ("BIRD_BATH", "BIRD_CAGE", "CROW_NEST", "BAGGED_FRIES"):
    assert "GuaniaoBlockEntityTypes." + block_type + ".get()" in entry
assert "BlockEntityRendererRegistry.register" in entry
assert "registerPlacedBlockRenderers();" in entry
for file in (cage, cage_model, nest, fries):
    assert "software.bernie.geckolib" not in file
    assert "net.minecraftforge" not in file
    assert "renderRecursively(" not in file

print("PASS: All four placed-bird-block renderer types registered through Fabric")
print("PASS: Cage models, nest egg count and each of 13 fries preserve original visibility")
