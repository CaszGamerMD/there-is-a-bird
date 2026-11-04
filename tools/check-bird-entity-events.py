#!/usr/bin/env python3
"""Baseline-driven checks for migrated server-side bird population/AI events.

This only verifies invariants in the source; complete gameplay tests are still required.
"""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
prefix = "src/main/java/EdDYON/guaniao/event/"
def original(name):
    return subprocess.check_output(["git", "show", "origin/main:" + prefix + name],
                                   cwd=root, text=True)
def migrated(name):
    value = (root / (prefix + name)).read_text("utf-8")
    if "net.minecraftforge" in value or "@SubscribeEvent" in value:
        raise SystemExit(f"{name}: Forge event listener still in migrated source")
    return value

old_pop = original("BirdPopulationTracker.java")
new_pop = migrated("BirdPopulationTracker.java")
# Preserve every explicitly calibrated numeric population/culling constant.
constants = re.compile(r"private static final (?:int|long|double|float) (\w+)\s*=\s*([^;]+);")
original_settings = dict(constants.findall(old_pop))
migrated_settings = dict(constants.findall(new_pop))
if original_settings != migrated_settings:
    raise SystemExit(f"Wild-bird population thresholds changed: {original_settings} -> {migrated_settings}")
for name in ("ServerEntityEvents.ENTITY_UNLOAD.register",
             "ServerLevelEvents.UNLOAD.register", "population.tracked.remove(entity.getUUID())",
             "LEVELS.remove(level)"):
    if name not in new_pop:
        raise SystemExit(f"Missing bird population cleanup: {name}")

old_threat = original("CassowaryThreatEvents.java")
new_threat = migrated("CassowaryThreatEvents.java")
for must_keep in (
    "mob instanceof Zombie || mob instanceof AbstractSkeleton || mob instanceof Spider",
    "13.0F, 1.10D, 1.35D", "getAvailableGoals()", "getTarget() instanceof CassowaryEntity",
    "Math.floorMod(mob.tickCount + mob.getId(), 20)"):
    if must_keep not in old_threat or must_keep not in new_threat:
        raise SystemExit(f"Cassowary hostile AI behavior was changed: {must_keep}")
for event in ("ServerEntityEvents.ENTITY_LOAD.register", "ServerEntityEvents.ENTITY_UNLOAD.register",
              "ServerTickEvents.END_LEVEL_TICK.register", "ServerLevelEvents.UNLOAD.register"):
    if event not in new_threat:
        raise SystemExit(f"Cassowary goal/target cleanup missing: {event}")

old_achieve = original("BirdAdvancementEvents.java")
new_achieve = migrated("BirdAdvancementEvents.java")
for value in ("NEARBY_BIRD_RADIUS = 24.0D", "TIT_LINE_RADIUS = 14.0D",
              "GUIDE_OBSERVE_DISTANCE = 28.0D", "staggeredTick % 40",
              "staggeredTick % 10", "BirdAdvancements.BIRD_CONFERENCE",
              "BirdAdvancements.FLUFFY_LINE", "BirdAdvancements.HIGH_EYE"):
    if value not in old_achieve or value not in new_achieve:
        raise SystemExit(f"Bird advancement condition/cadence lost: {value}")
if "ServerTickEvents.END_SERVER_TICK.register" not in new_achieve or "onPlayerTick(player)" not in new_achieve:
    raise SystemExit("Bird advancement polling isn't wired to Fabric end-server ticks.")

bootstrap = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text("utf-8")
for callback in ("BirdPopulationTracker", "CassowaryThreatEvents", "BirdAdvancementEvents"):
    if callback + ".registerCallbacks();" not in bootstrap:
        raise SystemExit(f"{callback} isn't attached to the Fabric initializer")
print("ENTITY EVENTS OK: original wild population limits, cassowary avoidance and all three bird advancements.")
