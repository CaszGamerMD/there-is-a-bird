#!/usr/bin/env python3
"""Preserve Nikon item capture behavior and every authored GeckoLib item pose."""

from pathlib import Path
import subprocess


item_path = "src/main/java/EdDYON/guaniao/content/camera/NikonD750Item.java"
renderer_path = "src/main/java/EdDYON/guaniao/client/camera/NikonD750ItemRenderer.java"
model_path = "src/main/java/EdDYON/guaniao/client/camera/NikonD750ItemModel.java"
entry_path = "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricClientEntrypoint.java"


def original(path: str) -> str:
    return subprocess.check_output(["git", "show", f"origin/main:{path}"], text=True).replace("\r\n", "\n")


item = Path(item_path).read_text()
renderer = Path(renderer_path).read_text()
model = Path(model_path).read_text()
entry = Path(entry_path).read_text()
old_renderer = original(renderer_path)
old_item = original(item_path)

assert "implements GeoItem" in item
assert "com.geckolib.animatable.GeoItem" in item
assert "com.geckolib.animatable.client.GeoRenderProvider" in item
assert "createGeoRenderer(Consumer<GeoRenderProvider> consumer)" in item
assert "consumer.accept(this.geoRenderProvider.getValue())" in item
assert "registerNikonCameraRenderer();" in entry
assert "GuaniaoItems.NIKON_D750.get()" in entry
assert "Suppliers.memoize(NikonD750ItemRenderer::new)" in entry
assert "NikonD750ItemRenderer extends GeoItemRenderer<NikonD750Item>" in renderer

# The original idle camera use and client viewfinder action are retained.
for phrase in [
    "player.getCooldowns().isOnCooldown(",
    "SoundEvents.UI_BUTTON_CLICK.value()",
    "SoundSource.PLAYERS, 0.7F, 1.35F",
    '"EdDYON.guaniao.client.camera.CameraClientCapture"',
    '"openViewfinder", InteractionHand.class',
    ".invoke(null, hand)",
]:
    assert phrase in old_item and phrase in item, phrase
assert "InteractionResult.FAIL" in item
assert "level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME" in item
assert "DistExecutor" not in item and "net.minecraftforge" not in item

# The old authored camera presentation deliberately has different transforms
# for inventory, hand, ground and frames; keep them verbatim.
marker = "    private static void applyDisplayTransform("
assert old_renderer[old_renderer.index(marker):] == renderer[renderer.index(marker):], (
    "Original camera display transforms, first-person single-hand offsets or 1.5x scale changed"
)
assert "getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE)" in renderer
assert "super.adjustRenderPose(info)" in renderer
assert "preRender(" not in renderer and "software.bernie.geckolib" not in renderer

for original_asset in ["geo/nikon_d750.geo.json", "textures/item/nikon_d750.png",
                       "animations/nikon_d750.animation.json"]:
    assert original_asset in model
assert "GuaniaoGeckoAssets.model(MODEL)" in model
assert "GuaniaoGeckoAssets.animation(ANIMATION)" in model

print("PASS: Nikon camera item keeps original sound, use and client viewfinder action")
print("PASS: GeckoLib 5 camera model and all original authored display transforms retained")
print("NOTE: Forge-only third-person CROSSBOW_HOLD arm-pose override needs a separate Fabric hook")
