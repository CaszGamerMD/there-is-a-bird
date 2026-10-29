#!/usr/bin/env python3
"""Guard GeckoLib 5 bird render-state conversion without erasing mutation or scale."""

from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
BASE = "src/main/java/EdDYON/guaniao/"
MODELS = {
    "kiwi/Kiwi": True,
    "sparrow/Sparrow": True,
    "myna/Myna": True,
    "longtailedtit/LongTailedTit": True,
    "seagull/Seagull": False,
    "nightheron/NightHeron": False,
    "crow/Crow": False,
    "macaw/Macaw": True,
    "budgerigar/Budgerigar": True,
}

def original(path):
    return subprocess.check_output(["git", "show", "origin/main:"+BASE+path],
                                   cwd=ROOT, text=True).replace("\r\n", "\n")
def current(path):
    return (ROOT/(BASE+path)).read_text("utf-8").replace("\r\n", "\n")

helper = current("client/entity/mutation/BirdMutationRenderData.java")
for word in ("DataTicket<Identifier> MUTATION_TEXTURE", "DataTicket<Float> MODEL_SCALE",
             "renderState.addGeckolibData(MUTATION_TEXTURE, texture)",
             "renderState.getOrDefaultGeckolibData(MUTATION_TEXTURE, fallback)"):
    assert word in helper, word
assert "new HashMap" not in helper

texture_factory = current("client/entity/mutation/BirdMutationTextureFactory.java")
original_factory = original("client/entity/mutation/BirdMutationTextureFactory.java")
for token in ("RAINBOW_FRAME_COUNT = 24", "BirdMutation.RAINBOW", "LEUCISTIC",
              "MELANISTIC", "GOLDEN", "GOLDEN_PURE", "recolor(",
              "rainbowFrame(", "getRainbowTexture("):
    assert token in original_factory and token in texture_factory, token
assert "net.minecraft.resources.Identifier" in texture_factory
assert "new ResourceLocation(" not in texture_factory

for stem, converted_renderer in MODELS.items():
    species = stem.split("/")[1]
    model_path = f"client/entity/{stem}Model.java"
    prev, model = original(model_path), current(model_path)
    assert "BirdMutationTextureFactory.textureFor(" in prev
    assert "BirdMutationTextureFactory.textureFor(" in model
    assert "BirdMutationRenderData.capture(renderState," in model
    assert "BirdMutationRenderData.texture(renderState," in model
    assert "getModelResource(GeoRenderState renderState)" in model
    assert "getTextureResource(GeoRenderState renderState)" in model
    assert "getAnimationResource(" in model
    assert "software.bernie" not in model
    assert "ResourceLocation" not in model
    assert species + "Definition.MODEL" in model
    assert species + "Definition.ANIMATION" in model

    if not converted_renderer:
        continue
    renderer_path = f"client/entity/{stem}Renderer.java"
    original_renderer, renderer = original(renderer_path), current(renderer_path)
    assert "getModelRenderScale()" in original_renderer
    assert "getModelRenderScale()" in model
    assert "MODEL_SCALE" in model
    assert "getOrDefaultGeckolibData(" in renderer
    assert "scaleModelForRender(RenderPassInfo<R>" in renderer
    assert "super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale)" in renderer
    assert "software.bernie" not in renderer
    before_radius = re.search(r"this\.shadowRadius = [^;]+;", original_renderer)
    assert before_radius and before_radius.group(0) in renderer

# The two columbid species share one model and resolve their actual
# gray/white/spotted palette from each individual entity in the render state.
columbid_old = original("client/entity/columbid/ColumbidModel.java")
columbid = current("client/entity/columbid/ColumbidModel.java")
for marker in (
    "GeoModel<T>", "ColumbidDefinition.MODEL", "ColumbidDefinition.ANIMATION",
    "GuaniaoGeckoAssets.model(", "GuaniaoGeckoAssets.animation(",
    "BirdMutationTextureFactory.textureFor(animatable.getTextureResource(), animatable)",
    "BirdMutationRenderData.capture(renderState,",
    "BirdMutationRenderData.texture(renderState,",
    "BirdMutationRenderData.MODEL_SCALE",
    "animatable.getModelRenderScale()",
):
    assert marker in columbid, marker
assert "BirdMutationTextureFactory.textureFor(animatable.getTextureResource(), animatable)" in columbid_old
assert "software.bernie" not in columbid
assert "ResourceLocation" not in columbid
entity = current("content/bird/columbid/AbstractColumbidEntity.java")
assert "public net.minecraft.resources.Identifier getTextureResource()" in entity
assert "return this.getColumbidVariant().texture();" in entity
for species, radius in (("Pigeon", "0.15F"), ("SpottedDove", "0.14F")):
    path = f"client/entity/columbid/{species}Renderer.java"
    before, now = original(path), current(path)
    assert "getModelRenderScale()" in before
    assert "new ColumbidModel<>()" in now
    assert "this.shadowRadius = " + radius + ";" in now
    assert "BirdMutationRenderData.MODEL_SCALE" in now
    assert "getOrDefaultGeckolibData(" in now
    assert "scaleModelForRender(RenderPassInfo<R>" in now
    assert "super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale)" in now
    assert "software.bernie" not in now

# Cockatoo toes need the original authored 0.751-pixel offset as well as
# its bird-specific mutation palette, without using the removed withScale API.
cockatoo_old = original("client/entity/cockatoo/CockatooModel.java")
cockatoo = current("client/entity/cockatoo/CockatooModel.java")
for marker in ("GuaniaoGeckoAssets.model(CockatooDefinition.MODEL)",
               "GuaniaoGeckoAssets.animation(CockatooDefinition.ANIMATION)",
               "BirdMutationRenderData.capture(renderState,",
               "BirdMutationTextureFactory.textureFor(animatable.getTextureResource(), animatable)",
               "BirdMutationRenderData.texture(renderState, CockatooDefinition.TEXTURE)",
               "animatable.getModelRenderScale()"):
    assert marker in cockatoo, marker
assert "BirdMutationTextureFactory.textureFor(bird.getTextureResource(), bird)" in cockatoo_old
assert "ResourceLocation" not in cockatoo and "software.bernie" not in cockatoo
cockatoo_entity = current("content/bird/cockatoo/CockatooEntity.java")
assert "public Identifier getTextureResource()" in cockatoo_entity
cockatoo_renderer = current("client/entity/cockatoo/CockatooRenderer.java")
previous_cockatoo = original("client/entity/cockatoo/CockatooRenderer.java")
assert "CockatooDefinition.FOOT_OFFSET_PIXELS * scale / 16.0F" in previous_cockatoo
assert "CockatooDefinition.FOOT_OFFSET_PIXELS / 16.0F" in cockatoo_renderer
assert "public void adjustRenderPose(RenderPassInfo<R> renderPassInfo)" in cockatoo_renderer
assert "super.adjustRenderPose(renderPassInfo)" in cockatoo_renderer
assert "scaleModelForRender(RenderPassInfo<R>" in cockatoo_renderer
assert "super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale)" in cockatoo_renderer
assert "this.shadowRadius = 0.22F;" in cockatoo_renderer
assert "software.bernie" not in cockatoo_renderer
assert "withScale(" not in cockatoo_renderer

registry = current("fabric/GuaniaoFabricEntityRenderers.java")
client = current("fabric/GuaniaoFabricClientEntrypoint.java")
assert "GuaniaoFabricEntityRenderers.register();" in client
registered = {
    "KIWI": "Kiwi",
    "SPARROW": "Sparrow",
    "MYNA": "Myna",
    "LONG_TAILED_TIT": "LongTailedTit",
    "MACAW": "Macaw",
    "BUDGERIGAR": "Budgerigar",
    "PIGEON": "Pigeon",
    "SPOTTED_DOVE": "SpottedDove",
    "COCKATOO": "Cockatoo",
}
for entity, name in registered.items():
    assert "EntityRendererRegistry.register(GuaniaoEntityTypes." + entity + ".get(), " + name + "Renderer::new)" in registry
assert registry.count("EntityRendererRegistry.register(") == len(registered)
assert "net.minecraftforge" not in registry
print("GECKOLIB 5 BIRD STATE PARITY OK: 11 model texture snapshots, 9 renderer scales and client registrations, unmodified mutation color formulas")
