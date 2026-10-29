#!/usr/bin/env python3
"""Guard mechanical 26.2 spawn-reason and gamerule changes without changing bird ecology."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PREVIOUS_REF = "dbeee5f10fc389aeba7cfe9c08ee35a828021e2e"
KINGFISHER = "src/main/java/EdDYON/guaniao/content/bird/kingfisher/KingfisherHabitat.java"
FLYBY = "src/main/java/EdDYON/guaniao/event/BirdFlybySpawnEvents.java"

def previous(path):
    return subprocess.check_output(["git", "show", PREVIOUS_REF + ":" + path],
                                   cwd=ROOT, text=True).replace("\r\n", "\n")

def current(path):
    return (ROOT / path).read_text("utf-8").replace("\r\n", "\n")

expected_kingfisher = previous(KINGFISHER).replace("MobSpawnType", "EntitySpawnReason").replace(
    "EntitySpawnReason.SPAWN_EGG", "EntitySpawnReason.SPAWN_ITEM_USE")
assert current(KINGFISHER) == expected_kingfisher, "Kingfisher habitat scan or manual-spawn bypass changed"
assert "EntitySpawnReason.COMMAND" in current(KINGFISHER)

expected_flyby = previous(FLYBY).replace("MobSpawnType", "EntitySpawnReason")
expected_flyby = expected_flyby.replace(
    "import net.minecraft.world.level.GameRules;",
    "import net.minecraft.world.level.gamerules.GameRules;")
expected_flyby = expected_flyby.replace(
    "level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)",
    "level.getGameRules().get(GameRules.SPAWN_MOBS)")
expected_flyby = expected_flyby.replace(
    "EntitySpawnReason.NATURAL, null, null)",
    "EntitySpawnReason.NATURAL, null)")
assert expected_flyby == current(FLYBY), "Flyby flock choice, travel path or spawn policy changed"
assert current(FLYBY).count("EntitySpawnReason.NATURAL, null)") == 4
assert "MobSpawnType" not in current(KINGFISHER) + current(FLYBY)
print("PASS: four original flyby initialization paths and manual kingfisher spawn exemptions preserved")
