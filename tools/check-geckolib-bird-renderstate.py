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
    "seagull/Seagull": True,
    "nightheron/NightHeron": True,
    "kestrel/Kestrel": True,
    "kingfisher/Kingfisher": True,
    "crow/Crow": True,
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
    # Kingfisher names its render-pass variable "pass"; verify the exact
    # multiplication and super call irrespective of the local variable name.
    assert re.search(
        r"super\.scaleModelForRender\((?:renderPassInfo|pass), "
        r"widthScale \* scale, heightScale \* scale\)", renderer
    ), (species, "original per-bird scale multiplier changed")
    assert "software.bernie" not in renderer
    # Equivalent 'shadowRadius' and 'this.shadowRadius' forms are both valid.
    # Compare the actual authored radius, not an incidental Java qualifier.
    before_radius = re.search(r"(?:this\.)?shadowRadius = [^;]+;", original_renderer)
    after_radius = re.search(r"(?:this\.)?shadowRadius = [^;]+;", renderer)
    assert before_radius and after_radius, (species, "missing shadow radius")
    assert before_radius.group(0).removeprefix("this.") == after_radius.group(0).removeprefix("this."), (species, "shadow radius changed")

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

# Crest rotations retain their original animation-relative offsets,
# rather than replacing the whole animated bone or sharing live bird state.
cockatiel_model = current("client/entity/cockatiel/CockatielModel.java")
cockatiel_before = original("client/entity/cockatiel/CockatielModel.java")
cockatiel_renderer = current("client/entity/cockatiel/CockatielRenderer.java")
for token in ("case RELAXED -> 0.0F", "case HAPPY -> -0.12F",
              "case CURIOUS -> -0.22F", "case ALERT -> -0.38F",
              "case AFRAID -> -0.56F"):
    assert token in cockatiel_before and token in cockatiel_model, token
assert "DataTicket<Float> CREST_ROTATION" in cockatiel_model
assert "renderState.addGeckolibData(CREST_ROTATION, offset)" in cockatiel_model
assert "BirdMutationTextureFactory.textureFor(animatable.getTextureResource(), animatable)" in cockatiel_model
assert "BirdMutationRenderData.capture(renderState," in cockatiel_model
assert "GuaniaoGeckoAssets.model(CockatielDefinition.MODEL)" in cockatiel_model
assert "GuaniaoGeckoAssets.animation(CockatielDefinition.ANIMATION)" in cockatiel_model
assert "public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots)" in cockatiel_renderer
assert "snapshots.get(\"crest\").ifPresent" in cockatiel_renderer
assert "crest.setRotX(crest.getRotX() + offset)" in cockatiel_renderer
assert "super.adjustModelBonesForRender(renderPassInfo, snapshots)" in cockatiel_renderer
assert "super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale)" in cockatiel_renderer
assert "this.shadowRadius = 0.12F;" in cockatiel_renderer
assert "public Identifier getTextureResource()" in current("content/bird/cockatiel/CockatielEntity.java")
assert "software.bernie" not in cockatiel_model + cockatiel_renderer

# Cassowary's seven-source-value head-led gaze remains weighted and
# animation-additive for head, upper neck and lower neck, never eye bones.
cassowary_old = original("client/entity/cassowary/CassowaryModel.java")
cassowary_model = current("client/entity/cassowary/CassowaryModel.java")
cassowary_renderer = current("client/entity/cassowary/CassowaryRenderer.java")
for bone in ('"head"', '"neckUpper"', '"neckLower"'):
    assert "addRotation(" + bone in cassowary_old
    assert "snapshots, " + bone in cassowary_renderer
for getter in ("getClientGazeWeight()", "getClientHeadPitch()", "getClientHeadYaw()",
               "getClientUpperNeckPitch()", "getClientUpperNeckYaw()",
               "getClientLowerNeckPitch()", "getClientLowerNeckYaw()"):
    assert getter in cassowary_old and getter in cassowary_model, getter
for name in ("HEAD_PITCH", "HEAD_YAW", "UPPER_NECK_PITCH", "UPPER_NECK_YAW",
             "LOWER_NECK_PITCH", "LOWER_NECK_YAW"):
    assert "DataTicket<Float> " + name in cassowary_model, name
    assert "CassowaryModel." + name in cassowary_renderer, name
for token in ("weight <= 0.001F", "Mth.DEG_TO_RAD",
              "BirdMutationRenderData.capture(renderState,",
              "BirdMutationRenderData.MODEL_SCALE",
              "GuaniaoGeckoAssets.model(CassowaryDefinition.MODEL)",
              "GuaniaoGeckoAssets.animation(CassowaryDefinition.ANIMATION)"):
    assert token in cassowary_model
assert "bone.setRotX(bone.getRotX() + pitch)" in cassowary_renderer
assert "bone.setRotY(bone.getRotY() + yaw)" in cassowary_renderer
assert "this.shadowRadius = 0.493F;" in cassowary_renderer
assert "software.bernie" not in cassowary_model + cassowary_renderer

# Woodcock's original controller-transition workaround forces the
# root_shape 0.45 scale AFTER animation and swaps walking/flying wing rigs.
woodcock_old = original("client/entity/woodcock/WoodcockModel.java")
woodcock_model = current("client/entity/woodcock/WoodcockModel.java")
woodcock_renderer = current("client/entity/woodcock/WoodcockRenderer.java")
for token in ('"root_shape"', '"wing_left"', '"wing_right"', '"fly"',
              "WoodcockDefinition.MODEL_ROOT_SCALE"):
    assert token in woodcock_old and token in woodcock_renderer
assert "DataTicket<Boolean> FLYING" in woodcock_model
assert "renderState.addGeckolibData(FLYING, animatable.isFlying())" in woodcock_model
assert "BirdMutationRenderData.capture(renderState," in woodcock_model
assert "BirdMutationRenderData.MODEL_SCALE" in woodcock_model
assert "GuaniaoGeckoAssets.model(WoodcockDefinition.MODEL)" in woodcock_model
assert "GuaniaoGeckoAssets.animation(WoodcockDefinition.ANIMATION)" in woodcock_model
for token in ("root.setScale(", "wing.skipRender(flying)", "wing.skipChildrenRender(flying)",
              "wing.skipRender(!flying)", "wing.skipChildrenRender(!flying)"):
    assert token in woodcock_renderer, token
assert "this.shadowRadius = 0.16F;" in woodcock_renderer
assert "software.bernie" not in woodcock_model + woodcock_renderer

# Preserve GeckoLib 4's authored carried-item positions and food pose seeds.
# GeckoLib 5 must snapshot item-model render data before its per-bone submit.
for species, name, anchor, radius, constants in (
    ("crow", "Crow", "mouth_anchor", "0.17F", (
        "MOUTH_ANCHOR_Y = 12.475D / 16.0D",
        "MOUTH_ANCHOR_Z = -8.95D / 16.0D",
        "BASE_X_ROTATION = 90.0F", "BASE_Z_ROTATION = 150.0F",
        "X_ROTATION_VARIATION = 5.0F", "Z_ROTATION_VARIATION = 35.0F",
        "ITEM_SCALE = 0.80F", "getHeldFoodPoseSeed()", "seededUnit(",
    )),
    ("seagull", "Seagull", "foot_right", "0.20F", (
        "poseStack.translate(0.0D, 0.015D, -0.018D)",
        "Axis.XP.rotationDegrees(90.0F)",
        "Axis.ZP.rotationDegrees(145.0F)",
        "poseStack.scale(0.46F, 0.46F, 0.46F)",
    )),
):
    path = f"client/entity/{species}/{name}HeldFoodLayer.java"
    before, now = original(path), current(path)
    renderer = current(f"client/entity/{species}/{name}Renderer.java")
    assert anchor in before and anchor in now, species
    assert f"this.shadowRadius = {radius};" in renderer
    for token in ("BlockAndItemGeoLayer", "getRelevantBones(", "addRenderData(",
                  "RenderData.item(", "super.submitItemStackRender(",
                  "ItemDisplayContext.GROUND", "RenderUtil.createRenderStateForItem("):
        assert token in now, (species, token)
    assert "software.bernie" not in now and "MultiBufferSource" not in now
    for constant in constants:
        assert constant in before and constant in now, (species, constant)
assert "new CrowHeldFoodLayer<>(context, this)" in current("client/entity/crow/CrowRenderer.java")
assert "new SeagullHeldFoodLayer<>(context, this)" in current("client/entity/seagull/SeagullRenderer.java")

# The heron's delivery fish is deliberately smaller, with a shallower bill pose.
heron_old = original("client/entity/nightheron/NightHeronHeldFishLayer.java")
heron = current("client/entity/nightheron/NightHeronHeldFishLayer.java")
for token in (
    '"fish_anchor"', "MOUTH_ANCHOR_Y = 14.35D / 16.0D",
    "MOUTH_ANCHOR_Z = -12.85D / 16.0D", "BASE_X_ROTATION = 90.0F",
    "BASE_Z_ROTATION = 145.0F", "X_ROTATION_VARIATION = 4.0F",
    "Z_ROTATION_VARIATION = 22.0F", "ITEM_SCALE = 0.55F",
    "DELIVERY_ANCHOR_Y = 14.05D / 16.0D",
    "DELIVERY_ANCHOR_Z = -13.35D / 16.0D",
    "DELIVERY_SCALE = 0.34F", "hasDeliveryFish()", "getHeldFishPoseSeed()",
    "NightHeronEntity.isEdibleFishItem(stack)", "seededUnit(",
):
    assert token in heron_old and token in heron, token
for token in ("BlockAndItemGeoLayer", "getRelevantBones(", "addRenderData(",
              "RenderData.item(", "super.submitItemStackRender(",
              "RenderUtil.createRenderStateForItem(", "ItemDisplayContext.NONE",
              "ItemDisplayContext.GROUND", "DataTicket<Boolean> DELIVERY",
              "DataTicket<Integer> POSE_SEED"):
    assert token in heron, token
assert "software.bernie" not in heron
assert "new NightHeronHeldFishLayer<>(context, this)" in current(
    "client/entity/nightheron/NightHeronRenderer.java")

# GeckoLib 5 Kestrel retains original owner head-perching and flight pose,
# while extended-talon root and leg resets run after authored animation.
kestrel_model = current("client/entity/kestrel/KestrelModel.java")
kestrel_original_model = original("client/entity/kestrel/KestrelModel.java")
assert "hasExtendedTalons()" in kestrel_original_model
for token in ("DataTicket<Boolean> EXTENDED_TALONS",
              "animatable.hasExtendedTalons()",
              "BirdMutationRenderData.MODEL_SCALE",
              "GuaniaoGeckoAssets.model(KestrelDefinition.MODEL)",
              "GuaniaoGeckoAssets.animation(KestrelDefinition.ANIMATION)"):
    assert token in kestrel_model, token
kestrel_old_layer = original("client/entity/kestrel/KestrelHeldItemLayer.java")
kestrel_layer = current("client/entity/kestrel/KestrelHeldItemLayer.java")
for token in ('"root"', "0.018D, -0.095D", "90.0F",
              "135.0F", "0.46F", "getCarriedItem()"):
    assert token in kestrel_old_layer and token in kestrel_layer, token
for token in ("BlockAndItemGeoLayer", "getRelevantBones(",
              "addRenderData(", "RenderData.item(",
              "super.submitItemStackRender(",
              "RenderUtil.createRenderStateForItem("):
    assert token in kestrel_layer, token
assert "software.bernie" not in kestrel_layer + kestrel_model

# All Kestrel pose inputs are snapshotted before the deferred render pass.
original_kestrel_renderer = original("client/entity/kestrel/KestrelRenderer.java")
kestrel_renderer = current("client/entity/kestrel/KestrelRenderer.java")
for marker in ("shouldShowName(", "isLearningFetchItem()",
               "getRenderOffset(", "ownerPosition.add(bird.ownerHeadOffset(",
               "owner.yHeadRotO", "owner.xRotO", "bird.isFlying()",
               "bird.flightBank(partialTick)", "Mth.rotLerp(", "Mth.lerp(",
               "this.shadowRadius = 0.18F;"):
    assert marker in original_kestrel_renderer and marker in kestrel_renderer, marker
for marker in ("DataTicket<Vec3> OWNER_RENDER_OFFSET", "DataTicket<Boolean> OWNER_PERCHED",
               "DataTicket<Boolean> FLIGHT_ACTIVE", "DataTicket<Float> PITCH",
               "DataTicket<Float> BANK", "DataTickets.ENTITY_BODY_YAW",
               "getGeckolibData(OWNER_RENDER_OFFSET)",
               "super.getRenderOffset(renderState)", "super.applyRotations(",
               "super.adjustModelBonesForRender(",
               "getOrDefaultGeckolibData(KestrelModel.EXTENDED_TALONS, false)",
               'snapshots.get("root")', '"leg_left"', '"leg_right"',
               "bone.setTranslateY(0)", "bone.setRotX(0)",
               "bone.setScaleY(1)", "widthScale * scale, heightScale * scale",
               "new KestrelHeldItemLayer<>(context, this)"):
    assert marker in kestrel_renderer, marker
assert "software.bernie" not in kestrel_renderer
assert "net.minecraftforge" not in kestrel_renderer

# Hummingbird kept the authored tiny model, 0.06 shadow, head-local
# garden item and eight wing/bank Molang variables. GeckoLib 5 evaluates the
# variables against each bird instead of sharing a last-rendered global.
hummingbird_original_model = original("client/entity/hummingbird/HummingbirdModel.java")
hummingbird_model = current("client/entity/hummingbird/HummingbirdModel.java")
hummingbird_renderer = current("client/entity/hummingbird/HummingbirdRenderer.java")
hummingbird_original_renderer = original("client/entity/hummingbird/HummingbirdRenderer.java")
hummingbird_old_layer = original("client/entity/hummingbird/HummingbirdHeldItemLayer.java")
hummingbird_layer = current("client/entity/hummingbird/HummingbirdHeldItemLayer.java")
hummingbird_molang = current("client/entity/hummingbird/HummingbirdMolangQueries.java")
for token in ('"root"', '"all"', '"waist"', '"leg_left"', '"leg_right"',
              '"foot_left"', '"foot_right"', "HEAD_PERCH_SUPPORT_BONES"):
    assert token in hummingbird_original_model and token in hummingbird_model
for token in ("geo/hummingbird.geo.json", "textures/entity/hummingbird.png",
              "animations/hummingbird_runtime.animation.json"):
    assert token in hummingbird_original_model and token in hummingbird_model
assert "HEAD_PERCHED" in hummingbird_model and "getVehicle() instanceof Player owner" in hummingbird_model
for token in ("getRenderOffset(", "ownerHeadOffset(", "owner.yHeadRotO", "owner.xRotO",
              "Mth.rotLerp(", "this.shadowRadius = 0.06F;"):
    assert token in hummingbird_original_renderer and token in hummingbird_renderer
for token in ("DataTickets.ENTITY_BODY_YAW", "OWNER_RENDER_OFFSET", "OWNER_PITCH",
              "super.getRenderOffset(renderState)", "super.applyRotations(",
              "super.adjustModelBonesForRender(",
              "getOrDefaultGeckolibData(HummingbirdModel.HEAD_PERCHED, false)",
              "HummingbirdModel.HEAD_PERCH_SUPPORT_BONES",
              "bone.setRotation(0, 0, 0)", "bone.setTranslation(0, 0, 0)",
              "bone.setScale(1, 1, 1)", "new HummingbirdHeldItemLayer<>(context, this)"):
    assert token in hummingbird_renderer, token
for token in ("4.25 / 16.0", "-3.45 / 16.0", "rotationDegrees(-20)",
              "0.12F", "getHeldGardenItem()", 'ItemDisplayContext.NONE'):
    assert token in hummingbird_old_layer and token in hummingbird_layer
assert "BlockAndItemGeoLayer" in hummingbird_layer
assert "RenderUtil.createRenderStateForItem(" in hummingbird_layer
for variable in ("flight_time", "wing_stroke_time", "forward_blend",
                 "backward_blend", "lateral_blend", "wing_pitch",
                 "wing_yaw_bias", "bank_angle"):
    token = "variable." + variable
    assert token in hummingbird_original_model and token in hummingbird_molang, token
assert "MolangQueries.<HummingbirdEntity>setActorVariable(" in hummingbird_molang
assert "actor.animatable()" in hummingbird_molang
assert "actor.partialTick()" in hummingbird_molang
assert "MolangParser.INSTANCE" not in hummingbird_molang
assert "HummingbirdMolangQueries.register();" in current("fabric/GuaniaoFabricEntityRenderers.java")
assert "software.bernie" not in hummingbird_model + hummingbird_layer + hummingbird_renderer

# Kingfisher now uses GeckoLib 5 per-body-bone rendering for dropping origin.
# Protect the original dip/swallow timing and animated anchor.
kingfisher_old = original("client/entity/kingfisher/KingfisherModel.java")
kingfisher_model = current("client/entity/kingfisher/KingfisherModel.java")
for key in ("pulse(tick, 11, 5)", "pulse(tick, 22, 5)",
            "pulse(tick, 38, 11)", "-26 * handling + 14 * swallow",
            "value * value * (3 - 2 * value)"):
    assert key in kingfisher_old and key in kingfisher_model
for key in ("DataTicket<Float> HEAD_DIP", "DataTicket<Float> BODY_DIP",
            "BirdMutationRenderData.MODEL_SCALE",
            "GuaniaoGeckoAssets.model(KingfisherDefinition.MODEL)",
            "GuaniaoGeckoAssets.animation(KingfisherDefinition.ANIMATION)"):
    assert key in kingfisher_model
kingfisher_layer_old = original("client/entity/kingfisher/KingfisherHeldFishLayer.java")
kingfisher_layer = current("client/entity/kingfisher/KingfisherHeldFishLayer.java")
for key in ("getHeldFish()", "6.35D + 0.25D * swallowing",
            "-6.05D + 1.65D * swallowing",
            "-20 - 45 * swallowing", "0.17F * (1 - swallowing)",
            "swallowing >= 0.999F"):
    assert key in kingfisher_layer_old and key in kingfisher_layer
for key in ("BlockAndItemGeoLayer", "RenderData.item(", "ItemDisplayContext.NONE",
            "RenderUtil.createRenderStateForItem(", "super.submitItemStackRender("):
    assert key in kingfisher_layer
assert "software.bernie" not in kingfisher_layer + kingfisher_model
assert "GuaniaoEntityTypes.KINGFISHER.get()" in current("fabric/GuaniaoFabricEntityRenderers.java")

kingfisher_renderer = current("client/entity/kingfisher/KingfisherRenderer.java")
kingfisher_renderer_old = original("client/entity/kingfisher/KingfisherRenderer.java")
for token in ("FOOT_OFFSET_PIXELS = -0.006486F", "3.75D / 16.0D",
              "0.3D / 16.0D", "rotationDegrees(-pitch)", "2.65F",
              "2.70F", "setDroppingRenderOrigin(", "getActivity() == KingfisherEntity.Activity.DEFECATE",
              "shadowRadius = 0.10F"):
    assert token in kingfisher_renderer_old and token in kingfisher_renderer, token
for token in ("BirdMutationRenderData.MODEL_SCALE", "new KingfisherHeldFishLayer<>(context, this)",
              "adjustModelBonesForRender(", "KingfisherModel.HEAD_DIP",
              "KingfisherModel.BODY_DIP", 'snapshots.get("head")',
              'snapshots.get("upbody")', "preRenderPass(", 'getBone("body")',
              "pass.addPerBoneRender(bone,", "RenderUtil.extractPoseFromRoot(",
              "getPreRenderMatrixState()", "getGeckolibData(DROPPING_SOURCE)",
              "getGeckolibData(DROPPING_POSITION)", "posed.renderState().getPartialTick()"):
    assert token in kingfisher_renderer, token
assert "software.bernie" not in kingfisher_renderer
assert "MultiBufferSource" not in kingfisher_renderer

# Umbrella Cockatoo: validate the last renderer and its expression continuity,
# owner-head rotation, per-bird pose snapshot, and derived animation paths.
umbrella_model = current("client/entity/umbrellacockatoo/UmbrellaCockatooModel.java")
umbrella_renderer = current("client/entity/umbrellacockatoo/UmbrellaCockatooRenderer.java")
umbrella_original_renderer = original("client/entity/umbrellacockatoo/UmbrellaCockatooRenderer.java")
umbrella_original_model = original("client/entity/umbrellacockatoo/UmbrellaCockatooModel.java")
assert "getModelRenderScale()" in umbrella_original_renderer
assert "getModelRenderScale()" in umbrella_model
for token in ("this.shadowRadius = 0.20F;",
              "ownerPosition.add(bird.ownerPerchOffset(", "owner.yBodyRotO",
              "owner.yHeadRotO", "owner.xRotO", "Mth.rotLerp("):
    assert token in umbrella_original_renderer and token in umbrella_renderer, token
for token in ("samplePose(", "getExpressionLevel()", "getCockatooAnimationTick()",
              "needsDisplayReference()", "getHeadRoll()",
              "expressions.clear()", "resourceVersion = version"):
    assert token in umbrella_original_model and token in umbrella_model, token
for token in ("BirdMutationRenderData.MODEL_SCALE", "BirdMutationRenderData.capture(",
              "GuaniaoGeckoAssets.model(UmbrellaCockatooDefinition.MODEL)",
              "GuaniaoGeckoAssets.animation(UmbrellaCockatooDefinition.ANIMATION)",
              "getBakedAnimation(", "CockatooOwnerPerchAnimation.create(",
              "CockatooDisplayResidual.create(", "EXPRESSION_POSE"):
    assert token in umbrella_model, token
for token in ("DataTickets.ENTITY_BODY_YAW", "OWNER_RENDER_OFFSET",
              "OWNER_PITCH", "super.getRenderOffset(state)",
              "super.applyRotations(", "adjustModelBonesForRender(",
              "UmbrellaCockatooModel.EXPRESSION_POSE",
              "CockatooPoseComposer.apply(snapshots, frame)"):
    assert token in umbrella_renderer, token
for name in ("CockatooPoseComposer", "CockatooDisplayResidual", "CockatooOwnerPerchAnimation"):
    now = current("client/entity/umbrellacockatoo/" + name + ".java")
    assert "software.bernie" not in now
    assert "com.geckolib." in now
pose_composer = current("client/entity/umbrellacockatoo/CockatooPoseComposer.java")
for token in ("BoneSnapshots", "headRoll * 0.35",
              "Math.toRadians(r.x())", "Math.toRadians(r.y())",
              "Math.toRadians(r.z())", "setTranslateX(",
              "setTranslateY(", "setTranslateZ("):
    assert token in pose_composer, token
residual = current("client/entity/umbrellacockatoo/CockatooDisplayResidual.java")
perch = current("client/entity/umbrellacockatoo/CockatooOwnerPerchAnimation.java")
assert 'NAME = "animation.idle_diff_2_residual"' in residual
assert "reference.bone(bone.boneName())" in residual
assert "source.keyframeMarkers()" in residual
assert "source.loopType()" in residual
assert "getUsedVariables()" in residual
assert 'NAME = "animation.owner_perch"' in perch
assert "model.boneLookup().get().keySet()" in perch
assert "new Constant(value)" in perch

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
    "COCKATIEL": "Cockatiel",
    "CASSOWARY": "Cassowary",
    "WOODCOCK": "Woodcock",
    "CROW": "Crow",
    "SEAGULL": "Seagull",
    "NIGHT_HERON": "NightHeron",
    "KESTREL": "Kestrel",
    "HUMMINGBIRD": "Hummingbird",
    "KINGFISHER": "Kingfisher",
    "UMBRELLA_COCKATOO": "UmbrellaCockatoo",
}
for entity, name in registered.items():
    assert "EntityRendererRegistry.register(GuaniaoEntityTypes." + entity + ".get(), " + name + "Renderer::new)" in registry
# The feather fan is not a bird species, but now has its own 26.2 renderer.
# Keep all 19 bird entries mandatory while accounting for this registered projectile.
assert "EntityRendererRegistry.register(GuaniaoEntityTypes.FEATHER_FAN_PROJECTILE.get(), FeatherFanProjectileRenderer::new)" in registry
assert registry.count("EntityRendererRegistry.register(") == len(registered) + 1
assert "net.minecraftforge" not in registry
print("GECKOLIB 5 BIRD STATE PARITY OK: 15 mutation bird models plus hummingbird, 19 renderer registrations, unmodified mutation color formulas")
