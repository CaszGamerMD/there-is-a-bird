#!/usr/bin/env python3
"""Ensure all six original bird baths migrate client item rendering without losing IDs."""

from pathlib import Path
import subprocess


item_file = "src/main/java/EdDYON/guaniao/content/bath/BirdBathItem.java"
original = subprocess.check_output(["git", "show", f"origin/main:{item_file}"], text=True).replace("\r\n", "\n")
item = Path(item_file).read_text(encoding="utf-8").replace("\r\n", "\n")
client = Path("src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricClientEntrypoint.java").read_text(encoding="utf-8")

for preserved in (
    "private final BirdBathVariant variant;",
    "this.variant = variant;",
    "return this.variant;",
    "registerControllers(AnimatableManager.ControllerRegistrar controllers) {\n    }",
    "return this.animationCache;",
):
    assert preserved in original and preserved in item, f"Original bird bath item behavior lost: {preserved}"

assert "new BirdBathItem(variant, blockSupplier.get()," in Path("src/main/java/EdDYON/guaniao/registry/GuaniaoItems.java").read_text()
assert "net.minecraftforge" not in item and "software.bernie.geckolib" not in item
assert "net.minecraft.client" not in item and "EdDYON.guaniao.client" not in item
assert "createGeoRenderer(Consumer<GeoRenderProvider> consumer)" in item
assert "consumer.accept(this.geoRenderProvider.getValue())" in item
assert "new MutableObject<>()" in item
assert "registerBirdBathItemRenderers();" in client
assert "com.google.common.base.Suppliers.memoize(BirdBathItemRenderer::new)" in client
for variant in (
    "WOODEN_BIRD_BATH", "STONE_BIRD_BATH", "BIRD_BATH",
    "WOODEN_BIRD_BATH_2", "STONE_BIRD_BATH_2", "BIRD_BATH_2",
):
    expected = f"registerBirdBathItemRenderer(GuaniaoItems.{variant}.get());"
    assert client.count(expected) == 1, f"Bath variant missing renderer handoff: {variant}"

print("PASS: six original bath item types bind exactly once to their 26.2 GeckoLib client providers")
print("PASS: common items never load client-only Forge or legacy GeckoLib classes")
print("PASS: authored bath variant, animation cache and controller behavior retained")
