#!/usr/bin/env python3
"""Check the 26.2 weighted-list conversion against the original Forge bird rules."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PREFIX = "src/main/java/EdDYON/guaniao/event/"

def original(file):
    return subprocess.check_output(
        ["git", "show", "origin/main:" + PREFIX + file],
        cwd=ROOT, text=True).replace("\r\n", "\n")

def migrated(file):
    return (ROOT / (PREFIX + file)).read_text("utf-8").replace("\r\n", "\n")

original_config = original("BirdSpawnConfigEvents.java")
config = migrated("BirdSpawnConfigEvents.java")
old_repopulation = original("BirdRepopulationEvents.java")
repopulation = migrated("BirdRepopulationEvents.java")

for text in (config, repopulation):
    assert "net.minecraftforge" not in text
    assert "ForgeEventFactory" not in text
    assert "WeightedRandomList" not in text

for token in ("MAX_CACHED_SPAWN_ENTRIES = 512",
              "BirdConfigManager.spawnMultiplier(species)",
              "multiplier <= 0.0D || available <= 0",
              "Math.min(3.0D, gardenMultiplier)",
              "Math.max(1.0D, Math.min(1.15D,",
              "BirdConfigManager.minGroup(species)",
              "BirdConfigManager.maxGroup(species)",
              "BirdColonySpawnRules.groupAt("):
    assert token in original_config and token in config, token

for token in ("BirdPopulationTracker.totalAt(level, x, z)",
              "BirdPopulationTracker.speciesAt(level, x, z, species)",
              "BirdPopulationTracker.totalInRegionAt(level, x, z)"):
    assert token in original_config and token in config, token

assert "List<Weighted<MobSpawnSettings.SpawnerData>>" in config
assert "WeightedList.of(entries)" in config
assert "entry.weight()" in repopulation
assert "original.weight() * multiplier" in config
assert "new Weighted<>(new MobSpawnSettings.SpawnerData(data.type(), minGroup, maxGroup), weight)" in config
assert "entries.add(original);" in config  # untouched modded/non-bird entries
assert "SPAWN_ENTRIES.computeIfAbsent" in config
assert "return original;" in config
assert "WeightedList.of(candidates).getRandom(level.getRandom())" in repopulation
assert repopulation.count("BirdSpawnConfigEvents.adjustedPotentialSpawns(") == 2
assert "atPosition.unwrap().stream().noneMatch(entry -> entry.value().type() == data.type())" in repopulation
assert "SpawnPlacements.isSpawnPositionOk(data.type(), level, pos)" in repopulation
assert "mob.checkSpawnRules(level, EntitySpawnReason.NATURAL)" in repopulation
assert "mob.checkSpawnObstruction(level)" in repopulation
assert "mob.getMaxSpawnClusterSize()" in repopulation
assert "mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos)," in repopulation
assert "data.type().create(level, EntitySpawnReason.NATURAL)" in repopulation
assert "data.type().getSpawnAABB(" in repopulation

constant = re.compile(r"private static final (?:double|int|long|float) ([A-Z_]+)\s*=\s*([^;]+);")
assert constant.findall(old_repopulation) == constant.findall(repopulation), "Authored spawn cadence or radius changed"
for token in (
    "BirdScanBudget.tryAcquireForSpecies(level, species)",
    "isActiveTime(species, level.getDayTime())",
    "2.0D - occupancy",
    "Math.min(MAX_PACK_SIZE, data.maxCount())",
    "Math.min(maxGroup, data.minCount())",
    "BirdPopulationTracker.totalAt(level, player.getX(), player.getZ())",
):
    assert token in repopulation, token
assert "TickEvent.LevelTickEvent" in old_repopulation
assert "LevelEvent.Unload" in old_repopulation
for token in ("ServerTickEvents.END_LEVEL_TICK.register", "ServerLevelEvents.UNLOAD.register"):
    assert token in repopulation, token
print("PASS: original spawn weights, habitat modifiers, caps, flock policy and cooldown constants retained")
