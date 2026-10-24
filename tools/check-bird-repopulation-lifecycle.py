#!/usr/bin/env python3
"""Verify original bird-habitat repopulation scheduling through Fabric level ticks."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
path = "src/main/java/EdDYON/guaniao/event/BirdRepopulationEvents.java"
old = subprocess.check_output(
    ["git", "show", "origin/main:" + path], cwd=root, text=True)
new = (root / path).read_text("utf-8")
head = "        long now = level.getGameTime();"
tail_old = "    @SubscribeEvent\n    public static void onLevelUnload"
tail_new = "    public static void onLevelUnload"
old_work = old[old.index(head):old.index(tail_old)]
new_work = new[new.index(head):new.index(tail_new)]
assert old_work == new_work, "Repopulation interval, chance, scan or cap calculation changed"
start = "    private static boolean isCrowded("
expected_body = old[old.index(start):].replace("MobSpawnType", "EntitySpawnReason")
expected_body = expected_body.replace(
    "getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)",
    "getGameRules().get(GameRules.SPAWN_MOBS)")
assert expected_body == new[new.index(start):], "Bird natural spawning/cap logic was modified"
assert "ServerTickEvents.END_LEVEL_TICK.register(BirdRepopulationEvents::onLevelTick);" in new
assert "ServerLevelEvents.UNLOAD.register((server, level) -> onLevelUnload(level));" in new
assert "ATTEMPTS.remove(level);" in new
assert "@Mod.EventBusSubscriber" not in new and "TickEvent.LevelTickEvent" not in new
initializer = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text("utf-8")
assert initializer.count("BirdRepopulationEvents.registerCallbacks();") == 1
print("PASS: native Fabric end-level tick and unload keep original bird repopulation work identical")
