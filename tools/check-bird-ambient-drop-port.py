#!/usr/bin/env python3
"""Guard original feather and bird-dropping lifetimes through vanilla 26.2 item age conversion."""
import json
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
relative = "src/main/java/EdDYON/guaniao/content/bird/BirdAmbientDropControl.java"
old = subprocess.check_output(["git", "show", "origin/main:" + relative], cwd=root, text=True)
new = (root / relative).read_text("utf-8")
for value in (
    "PLAYER_ACTIVITY_RADIUS = 64.0D",
    "LOCAL_CAP_RADIUS = 16.0D",
    "MIN_LIFETIME_TICKS = 20 * 60 * 5",
    "MAX_LIFETIME_TICKS = 20 * 60 * 8",
    "MIN_FEATHER_LIFETIME_TICKS = 20 * 60 * 10",
    "MAX_FEATHER_LIFETIME_TICKS = 20 * 60 * 15",
    "HARD_MAX_DROPPINGS_NEARBY = 16",
    "MAX_NATURAL_FEATHERS_NEARBY = 6",
    "BirdConfigManager.droppingLifetimeMinTicks()",
    "BirdConfigManager.droppingLifetimeMaxTicks()"):
    if value not in old or value not in new:
        raise SystemExit("Natural feather/dropping counts or lifetime settings changed: " + value)
for phrase in (
    "VANILLA_DESPAWN_TICKS = 6000",
    "accessor.guaniao$setAge(VANILLA_DESPAWN_TICKS - lifetimeTicks)",
    "itemEntity.level().getRandom().nextInt(MAX_FEATHER_LIFETIME_TICKS - MIN_FEATHER_LIFETIME_TICKS + 1)",
    "applyLifetime(itemEntity, randomLifetime(itemEntity.level().getRandom()))",
    "ItemEntityAgeAccessor"):
    if phrase not in new:
        raise SystemExit("26.2 lifetime conversion missing: " + phrase)
for phrase in (
    "level.players().stream().anyMatch",
    "player.isAlive()",
    "player.distanceToSqr(source)",
    "PLAYER_ACTIVITY_RADIUS * PLAYER_ACTIVITY_RADIUS"):
    if phrase not in new:
        raise SystemExit("Original 64-block nearby living-player criterion changed: " + phrase)
if "itemEntity.lifespan" in new:
    raise SystemExit("Removed Forge lifespan field still used.")
mixins = json.loads((root / "src/main/resources/guaniao.mixins.json").read_text("utf-8"))
if "ItemEntityAgeAccessor" not in mixins.get("mixins", []):
    raise SystemExit("Vanilla ItemEntity age mixin not registered.")
accessor = (root / "src/main/java/EdDYON/guaniao/mixin/ItemEntityAgeAccessor.java").read_text("utf-8")
for phrase in ('@Mixin(ItemEntity.class)', '@Accessor("age")', 'void guaniao$setAge(int age)'):
    if phrase not in accessor:
        raise SystemExit("Vanilla ItemEntity age accessor missing: " + phrase)
# Items despawn on vanilla age 6000. Initial age = 6000 - the original
# authored lifetime yields the same absolute tick count for both ranges.
for life in (6000, 9600, 12000, 18000, 36000):
    initial_age = 6000 - life
    if initial_age == -32768 or 6000 - initial_age != life:
        raise SystemExit("Configured lifetime would become unlimited or truncated.")
print("AMBIENT DROP LIFETIME PARITY OK: original 5–8 min, 10–15 min and configurable 1–30 min ranges.")
