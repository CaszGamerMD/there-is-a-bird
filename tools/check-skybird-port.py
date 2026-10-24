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
