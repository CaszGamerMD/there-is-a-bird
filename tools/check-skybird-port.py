#!/usr/bin/env python3
"""Confirm the seven original sky-bird species only change Identifier API."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
path = "src/main/java/EdDYON/guaniao/client/skybird/SkyBirdSpecies.java"
original = subprocess.check_output(["git", "show", "origin/main:" + path], cwd=root, text=True)
ported = (root / path).read_text(encoding="utf-8")
expected = original.replace("ResourceLocation", "Identifier").replace(
    "new Identifier(GuaniaoMod.MOD_ID, ", "Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, ")
if ported != expected:
    raise SystemExit("Sky-bird visual assets, flight configuration, or RNG differs from source")
for species in ("GOOSE", "EAGLE", "SEAGULL", "SWALLOW", "CRANE", "VULTURE", "STARLING"):
    if species + "(" not in ported:
        raise SystemExit("Missing sky-bird variant: " + species)
print("SKY-BIRD PARITY OK: 7 sprite IDs and original flock/flight parameters")

# The old Forge render-stage event and mutable MultiBufferSource path are replaced
# by an extraction snapshot followed by a 26.2 deferred SubmitNodeCollector pass.
renderer = (root / "src/main/java/EdDYON/guaniao/client/skybird/SkyBirdRenderer.java").read_text("utf-8")
events = (root / "src/main/java/EdDYON/guaniao/client/skybird/SkyBirdClientEvents.java").read_text("utf-8")
types = (root / "src/main/java/EdDYON/guaniao/client/skybird/SkyBirdRenderTypes.java").read_text("utf-8")
for token in ("LevelExtractionContext", "getGameTimeDeltaPartialTick(false)",
              "List.copyOf(vertices)", "private record Snapshot(", "cameraPosition",
              "animationFrame(", "distanceAlpha(", "viewAlpha(", "flock.lifecycleAlpha(partialTick)",
              "context.submitNodeCollector()", "submitCustomGeometry(", "LightCoordsUtil.FULL_BRIGHT"):
    assert token in renderer, token
for token in ("ClientTickEvents.END_CLIENT_TICK", "ClientPlayConnectionEvents.DISCONNECT",
              "LevelExtractionEvents.END_EXTRACTION", "LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN",
              "SkyBirdRenderer.clear()"):
    assert token in events, token
assert "RenderTypes.entityTranslucentEmissive(texture, false)" in types
for obsolete in ("net.minecraftforge.", "RenderLevelStageEvent", "MultiBufferSource",
                 "RenderStateShard", "class SkyBirdRenderTypes extends RenderType"):
    assert obsolete not in renderer + events + types, obsolete
