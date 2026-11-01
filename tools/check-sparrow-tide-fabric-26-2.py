#!/usr/bin/env python3
"""Sparrow-tide wave scheduling and caps must survive the Forge to Fabric event migration."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
REL = "src/main/java/EdDYON/guaniao/event/SparrowTideManager.java"
old = subprocess.check_output(["git", "show", "origin/main:" + REL], cwd=ROOT, text=True)
new = (ROOT / REL).read_text(encoding="utf-8")
init = (ROOT / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text(encoding="utf-8")

assert 'SparrowTideManager.registerCallbacks();' in init
for callback in ("ServerTickEvents.END_SERVER_TICK", "ServerLevelEvents.UNLOAD", "ServerLifecycleEvents.SERVER_STOPPING"):
    assert callback in new, callback
assert 'for (ServerLevel level : server.getAllLevels())' in new
assert 'level.dimension() != Level.OVERWORLD || level.getGameTime() % 5L != 0L' in new
assert 'level.getGameRules().get(GameRules.SPAWN_MOBS)' in new
assert 'EntitySpawnReason.NATURAL, null)' in new
assert 'level.getMinY()' in new and 'level.getMaxY()' in new
assert 'Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "sparrow_tide_habitat")' in new
assert 'level.getRandom()' in new and 'level.random' not in new
assert 'net.minecraftforge' not in new and '@SubscribeEvent' not in new

# The event handler is the only intended gameplay edit. All original tide
# formulas, probability checks, bounded spawns and observer rules are unchanged.
for code in ("0.08D * activity", "0.30F", "0.70F", "100, 200", "40 * 20, 120 * 20",
             "SparrowTideRules.WAVE_MIN, SparrowTideRules.WAVE_MAX",
             "SparrowTideRules.COOLDOWN_MIN, SparrowTideRules.COOLDOWN_MAX",
             "SparrowTideRules.EVENT_RADIUS * SparrowTideRules.EVENT_RADIUS",
             "SparrowTideRules.remainingCapacity(", "SparrowTideRules.eventsOverlap(",
             "BirdScanBudget.tryAcquire(level, 1)", "BirdPopulationTracker.totalAt(",
             "BirdPopulationTracker.totalInRegionAt(", "BirdFlybySpawnEvents.isHiddenFromNearbyPlayers(",
             "tide.waveAttempts = tide.waveRemaining * 4", "tide.waveAngle += 2.399963229728653D"):
    assert code in old and code in new, f"missing original tide behavior: {code}"

def constants(source):
    return dict(re.findall(r'private static final int (MAX_\w+) = (\d+);', source))

assert constants(old) == constants(new) == {"MAX_EVENTS_PER_LEVEL": "128", "MAX_ACTIVE_EVENTS": "2"}
assert old.count("level.random") == new.count("level.getRandom()")
assert old.count("tide.waveRemaining--") == new.count("tide.waveRemaining--") == 1
assert old.count("level.addFreshEntity(bird)") == new.count("level.addFreshEntity(bird)") == 1
assert old.count("if (!enabled || activity <= 0.0D") == new.count("if (!enabled || activity <= 0.0D") == 1
assert 'SparrowTideManager.registerCallbacks();' in init

proof = (ROOT / "tools/port-probes/GuaniaoSparrowTideProof.java").read_text(encoding="utf-8")
for signature in ("END_SERVER_TICK.register", "SERVER_STOPPING.register", "UNLOAD.register",
                  "GameRules.SPAWN_MOBS", "EntitySpawnReason.NATURAL"):
    assert signature in proof
print("PASS: Fabric sparrow tide tick, cleanup, gamerule and unchanged wave limits/odds")
