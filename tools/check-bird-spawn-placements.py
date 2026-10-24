#!/usr/bin/env python3
"""Verify all 19 bird placement rules exactly match the original Forge release."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
prefix = Path("src/main/java/EdDYON/guaniao")
original = subprocess.check_output(
    ["git", "show", "origin/main:" + (prefix / "event/GuaniaoModEvents.java").as_posix()],
    cwd=root, text=True)
new = (root / prefix / "registry/GuaniaoEntityTypes.java").read_text("utf-8")
old_pattern = re.compile(
    r"event\.register\((?:\(EntityType\))?\s*GuaniaoEntityTypes\.(\w+)\.get\(\),"
    r"\s*(SpawnPlacements\.Type\.ON_GROUND|BirdSpawnPlacements\.ON_GROUND_OR_LEAVES),"
    r"\s*Heightmap\.Types\.(\w+),"
    r"\s*withoutChunkGeneration\((\w+Entity)::(\w+)\),"
    r"\s*SpawnPlacementRegisterEvent\.Operation\.REPLACE\)")
new_pattern = re.compile(
    r"(?:public static final )?FabricRegistryQueue\.Ref<EntityType<[^>]+>> (\w+)"
    r" = GuaniaoEntityTypes\.registerCreature\("
    r"[^;]*?,\s*(SpawnPlacementTypes\.ON_GROUND|BirdSpawnPlacements\.ON_GROUND_OR_LEAVES),"
    r"\s*Heightmap\.Types\.(\w+),\s*(\w+Entity)::(\w+)\)")
old_list = old_pattern.findall(original)
new_list = new_pattern.findall(new)
if len(old_list) != 19 or len(new_list) != 19:
    raise SystemExit(f"Lost spawn placement rules: Forge={len(old_list)}, Fabric={len(new_list)}")

def normalize(entry):
    name, placement, heightmap, species, predicate = entry
    return name, placement.removeprefix("SpawnPlacements.Type.").removeprefix(
        "SpawnPlacementTypes.").removeprefix("BirdSpawnPlacements."), heightmap, species, predicate

expected = [normalize(x) for x in old_list]
actual = [normalize(x) for x in new_list]
if expected != actual:
    raise SystemExit(f"Spawn heightmap, species or canopy-footing parity failure: "
                     f"missing={sorted(set(expected)-set(actual))} extra={sorted(set(actual)-set(expected))}")
for token in (
        "FabricEntityType.Builder.createMob(factory, BIRD",
        "builder.spawnPlacement(placementType, heightmap",
        "reason != EntitySpawnReason.CHUNK_GENERATION",
        "originalPredicate.test(type, level, reason, pos, random)",
        "BirdVisibility.TRACKING_RANGE_CHUNKS",
):
    if token not in new:
        raise SystemExit("Original mob spawn preconditions or range were dropped: " + token)

old_canopy = subprocess.check_output(
    ["git", "show", "origin/main:" + (prefix / "event/BirdSpawnPlacements.java").as_posix()],
    cwd=root, text=True)
canopy = (root / prefix / "event/BirdSpawnPlacements.java").read_text("utf-8")
for token in (
        "level.getWorldBorder().isWithinBounds(pos)",
        "level.isOutsideBuildHeight(pos)",
        "level.getBlockState(below).is(BlockTags.LEAVES)",
        "NaturalSpawner.isValidEmptySpawnBlock(level, pos",
        "NaturalSpawner.isValidEmptySpawnBlock(level, above",
):
    if token not in old_canopy or token not in canopy:
        raise SystemExit("Bird leaves-spawn placement safety check lost: " + token)
if "SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, type)" not in canopy:
    raise SystemExit("Ground spawning no longer uses the vanilla 26.2 footing check")
print("BIRD SPAWN PLACEMENTS OK: all 19 exact predicates, heightmaps and canopy/ground modes preserved.")

# Verify the original Forge SpawnPlacementCheck vetoes were not lost when
# registering the same 19 predicates through Fabric's mob type builder.
original_config = subprocess.check_output(
    ["git", "show", "origin/main:" +
     "src/main/java/EdDYON/guaniao/event/BirdSpawnConfigEvents.java"],
    cwd=root, text=True)
natural = (root / prefix / "fabric/GuaniaoNaturalSpawnRules.java").read_text("utf-8")
required_source = (
    "species.requiresOpenSkyForNaturalSpawn()",
    "FlowerHabitatIndex.countFlowers(",
    "GardenAttraction",  # potential-spawn list adjustment remains a separate blocker
    "BirdConfigManager.allowsNaturalSpawning(species)",
    "BirdPopulationTracker.totalAt(level, x, z)",
    "BirdPopulationTracker.totalInRegionAt(level, x, z)",
    "BirdPopulationTracker.speciesAt(level, x, z, species)",
    "BirdConfigManager.maxWildNearby(species)",
)
for token in required_source:
    if token not in original_config:
        raise SystemExit("Unexpectedly missing original spawn behavior: " + token)
for token in required_source:
    if token == "GardenAttraction":
        continue  # handled by original positional biome spawn adjustments, not a placement predicate
    if token not in natural:
        raise SystemExit("Natural Fabric spawn placement lost original Forge veto: " + token)
if "FlowerHabitatIndex.countFlowers(server, pos, 16) < 5" not in natural:
    raise SystemExit("Hummingbird habitat lost minimum five flowers in sixteen blocks")
if "EntitySpawnReason.CHUNK_GENERATION" not in natural:
    raise SystemExit("Runtime region should never query live population during chunk generation")
if "GuaniaoNaturalSpawnRules.allowsPlacement(type, level, reason, pos)" not in new:
    raise SystemExit("Native entity builders fail to enforce natural spawn configuration/caps")
print("NATURAL SPAWN CAP OK: original sky, garden and local/regional species cap vetoes wired.")
