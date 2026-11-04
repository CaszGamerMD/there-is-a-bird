#!/usr/bin/env python3
"""Check the bird-brain state keys and safe typed NBT migration for Minecraft 26.2."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
base = "src/main/java/EdDYON/guaniao/content/bird/brain/"
files = {
    "BirdBrain.java": (root / (base + "BirdBrain.java")).read_text("utf-8"),
    "BirdMotivation.java": (root / (base + "BirdMotivation.java")).read_text("utf-8"),
    "BirdPersonality.java": (root / (base + "BirdPersonality.java")).read_text("utf-8"),
    "BirdSenses.java": (root / (base + "BirdSenses.java")).read_text("utf-8"),
}
for token in ('put("BirdBrain", brainTag)', 'put("Motivation", motivationTag)',
              'put("Personality", personalityTag)', 'putLong("Migration", this.nextMigrationTick)'):
    assert token in files["BirdBrain.java"], token
for token in ('getCompound("BirdBrain").orElse(null)',
              'getCompound("Motivation").ifPresent(this.motivation::load)',
              'getLong("Migration").orElse(null)',
              'getCompound("Personality").orElseGet(CompoundTag::new)',
              'isClientSide()'):
    assert token in files["BirdBrain.java"], token
assert 'tag.getFloat(key).orElse(fallback)' in files["BirdMotivation.java"]
for key in ("Boldness", "Wariness", "Activity", "Sociability", "Flightiness"):
    assert f'tag.getFloat("{key}").orElse(Float.NaN)' in files["BirdPersonality.java"], key
assert "getDefaultClockTime() % 24000L" in files["BirdSenses.java"]
for name, content in files.items():
    assert "contains(\"" not in content or ", " not in content, name
print("BIRD BRAIN 26.2 NBT PARITY OK: original keys/defaults, optional typed reads and dimension clock")
