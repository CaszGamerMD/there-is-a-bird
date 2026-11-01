#!/usr/bin/env python3
"""Check typed Fabric entity persistence and authored ambient bird intervals."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
package = "src/main/java/EdDYON/guaniao/"

def migrated(relative):
    return (root / package / relative).read_text("utf-8")

def original(relative):
    return subprocess.check_output(
        ["git", "show", "origin/main:" + package + relative], cwd=root, text=True)

attachments = migrated("content/bird/BirdEntityAttachments.java")
names = {
    "MOLT_COOLDOWN": ("molt_cooldown", "INT"),
    "DROPPING_COOLDOWN": ("dropping_cooldown", "INT"),
    "LAST_NEARBY_PLAYER_TIME": ("last_nearby_player_time", "LONG"),
    "TRANSIENT_FLYBY_SPAWN_TIME": ("transient_flyby_spawn_time", "LONG"),
    "INDIVIDUAL_WALK_FACTOR": ("individual_walk_factor", "FLOAT"),
}
for name, (path, codec) in names.items():
    pattern = (r"AttachmentType<\w+>\s+" + name + r"\s*=\s*AttachmentRegistry.createPersistent\(\s*"
               r'Identifier.fromNamespaceAndPath\("guaniao", "' + path + r'"\), Codec\.' + codec + r"\)")
    assert re.search(pattern, attachments), f"Missing persistent {name} ({codec})"
assert "BirdEntityAttachments.registerAttachments();" in migrated("fabric/GuaniaoFabricEntrypoint.java")
assert "include 'EdDYON/guaniao/content/bird/BirdEntityAttachments.java'" in (root / "build.gradle").read_text("utf-8")

for rel in ("event/BirdMoltEvents.java", "event/BirdDroppingEvents.java",
            "event/BirdPopulationTracker.java", "content/bird/BirdGroundLocomotion.java"):
    new = migrated(rel)
    assert "getPersistentData()" not in new, f"Forge entity NBT remains in {rel}"
    assert "import EdDYON.guaniao.content.bird.BirdEntityAttachments;" in new or rel.startswith("content/bird/")

for rel in ("event/BirdMoltEvents.java", "event/BirdDroppingEvents.java", "event/BirdPopulationTracker.java"):
    old, new = original(rel), migrated(rel)
    matches = re.compile(r"private static final (?:int|long|float|double) (\w+)\s*=\s*([^;]+);")
    old_constants = dict(matches.findall(old))
    new_constants = dict(matches.findall(new))
    assert old_constants == new_constants, f"Authored timing/threshold constants changed: {rel}"

molt = migrated("event/BirdMoltEvents.java")
for token in ("previousCooldown == null", "previousCooldown - CHECK_INTERVAL_TICKS",
              "if (tryMolt(entity))", "MOLT_MIN_TICKS, MOLT_MAX_TICKS",
              "RETRY_MIN_TICKS, RETRY_MAX_TICKS", "hasNearbyPlayer(level, entity)"):
    assert token in molt, f"Molt cadence or spawn behavior lost: {token}"

population = migrated("event/BirdPopulationTracker.java")
for token in ("lastNearbyPlayerTime == null", "lastNearbyPlayerTime = now",
              "mob.setAttached(BirdEntityAttachments.LAST_NEARBY_PLAYER_TIME, now)",
              "mob.setAttached(BirdEntityAttachments.TRANSIENT_FLYBY_SPAWN_TIME, level.getGameTime())",
              "mob.removeAttached(BirdEntityAttachments.LAST_NEARBY_PLAYER_TIME)",
              "flybySpawnTime != null", "elapsed(now, flybySpawnTime)",
              "elapsed(now, lastNearbyPlayerTime)"):
    assert token in population, f"Wild-bird retention or lifecycle timer lost: {token}"

walking = migrated("content/bird/BirdGroundLocomotion.java")
for token in ("savedFactor != null", "Float.isFinite(savedFactor)",
              "1.0F - profile.individualVariance", "1.0F + profile.individualVariance",
              "bird.setAttached(BirdEntityAttachments.INDIVIDUAL_WALK_FACTOR, individualFactor)"):
    assert token in walking, f"Individual walking speed persistence lost: {token}"
print("ENTITY ATTACHMENTS OK: five typed persistent fields, original drop/molt and culling behavior.")
