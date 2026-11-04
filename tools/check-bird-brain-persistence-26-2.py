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
extra = {
    "PigeonEntity.java": (root / "src/main/java/EdDYON/guaniao/content/bird/columbid/PigeonEntity.java").read_text("utf-8"),
    "WoodcockEntity.java": (root / "src/main/java/EdDYON/guaniao/content/bird/woodcock/WoodcockEntity.java").read_text("utf-8"),
    "BirdModelScale.java": (root / "src/main/java/EdDYON/guaniao/content/bird/scale/BirdModelScale.java").read_text("utf-8"),
}
for token in ('getInt("PigeonVariant").isEmpty()',
              'getInt("PigeonVariant").orElse(ColumbidVariant.GRAY_PIGEON.ordinal())',
              'create(level, EntitySpawnReason.BREEDING)'):
    assert token in extra["PigeonEntity.java"], token
for token in ('tag.getInt(TERRITORY_X_TAG)', 'tag.getInt(TERRITORY_Y_TAG)',
              'tag.getInt(TERRITORY_Z_TAG)', 'territoryX.isPresent()',
              'getInt(FORAGE_COOLDOWN_TAG).orElse(0)',
              'getInt(SPAWN_AWAKE_TICKS_TAG).orElse(0)',
              'getInt(SLEEP_COOLDOWN_TAG).orElse(0)'):
    assert token in extra["WoodcockEntity.java"], token
assert 'compoundTag.getFloat(NBT_KEY).orElse(DEFAULT_INDIVIDUAL_SCALE)' in extra["BirdModelScale.java"]
client_files = {
    "EarthwormItem.java": (root / "src/main/java/EdDYON/guaniao/content/earthworm/EarthwormItem.java").read_text("utf-8"),
    "BirdCommandInteraction.java": (root / "src/main/java/EdDYON/guaniao/content/bird/command/BirdCommandInteraction.java").read_text("utf-8"),
    "BirdBodyRotationControl.java": (root / "src/main/java/EdDYON/guaniao/content/bird/BirdBodyRotationControl.java").read_text("utf-8"),
}
assert "context.getLevel().isClientSide()" in client_files["EarthwormItem.java"]
assert "bird.level().isClientSide()" in client_files["BirdCommandInteraction.java"]
assert "this.bird.level().isClientSide()" in client_files["BirdBodyRotationControl.java"]
for name, content in client_files.items():
    assert ".isClientSide)" not in content, name
print("BIRD 26.2 PERSISTENCE/LEVEL API PARITY OK: original NBT defaults and client/server guards")
