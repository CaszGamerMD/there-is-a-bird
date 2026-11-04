#!/usr/bin/env python3
"""Ensure the Forge unload subscriber became Fabric without changing flock audio caps."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
FILE = "src/main/java/EdDYON/guaniao/content/bird/BirdFlockSoundLimiter.java"
old = subprocess.check_output(["git", "show", "origin/main:" + FILE], cwd=ROOT, text=True)
new = (ROOT / FILE).read_text(encoding="utf-8")

assert old.split("public static boolean allowAmbient(Entity bird) {")[1].split("@SubscribeEvent")[0] == \
    new.split("public static boolean allowAmbient(Entity bird) {")[1].split("public static void onLevelUnload")[0], \
    "flock sound limits, cooldowns or calculation changed"
assert old.split("private record SoundKey")[1] == new.split("private record SoundKey")[1], \
    "flock sound window keys, expiry, or trimming changed"
assert "net.minecraftforge" not in new and "@SubscribeEvent" not in new
assert "ServerLevelEvents.UNLOAD.register((server, level) -> onLevelUnload(level));" in new
assert "WINDOWS.remove(level)" in new
init = (ROOT / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text(encoding="utf-8")
assert "BirdFlockSoundLimiter.registerCallbacks();" in init
print("PASS: identical ambient-call budgets, per-species cooldowns and Fabric level-unload cleanup")
