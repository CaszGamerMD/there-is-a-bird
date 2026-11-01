#!/usr/bin/env python3
"""Guard unchanged wild-bird scan quota and droppings area limits after Fabric lifecycle port."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
prefix = "src/main/java/EdDYON/guaniao/content/bird/"

def source(name):
    old = subprocess.check_output(["git", "show", "origin/main:" + prefix + name], cwd=root, text=True)
    new = (root / prefix / name).read_text("utf-8")
    assert "net.minecraftforge" not in new and "@SubscribeEvent" not in new
    return old, new

scan_old, scan_new = source("BirdScanBudget.java")
scan_start = "    public static boolean tryAcquire(ServerLevel level) {"
scan_end_old = "    @SubscribeEvent\n    public static void onLevelUnload("
scan_end_new = "    public static void onLevelUnload(ServerLevel level) {"
assert scan_old[scan_old.index(scan_start):scan_old.index(scan_end_old)] == scan_new[
    scan_new.index(scan_start):scan_new.index(scan_end_new)], "Species/level scan quota changed"
scan_tail = "    public record Stats(int usedThisTick, long granted, long denied, long tick) {"
assert scan_old[scan_old.index(scan_tail):] == scan_new[scan_new.index(scan_tail):], "Scan token refill or stats changed"
for token in ("USAGE.remove(level)",
              "ServerLevelEvents.UNLOAD.register((server, level) -> onLevelUnload(level))"):
    assert token in scan_new

drop_old, drop_new = source("BirdDroppingAreaLimiter.java")
drop_start = "    public static boolean canDrop(ServerLevel level, BlockPos pos) {"
drop_end_old = "    @SubscribeEvent\n    public static void onServerTick("
drop_end_new = "    public static void onServerTick() {"
assert drop_old[drop_old.index(drop_start):drop_old.index(drop_end_old)] == drop_new[
    drop_new.index(drop_start):drop_new.index(drop_end_new)], "Area drop eligibility or timing changed"
tick_begin = "        Iterator<Map.Entry<ServerLevel, AreaWindows>> iterator = WINDOWS.entrySet().iterator();"
tick_end = "    public static void onLevelUnload("
old_tick_end = "    @SubscribeEvent\n    public static void onLevelUnload("
assert drop_old[drop_old.index(tick_begin):drop_old.index(old_tick_end)] == drop_new[
    drop_new.index(tick_begin):drop_new.index(tick_end)], "Area cache cleanup cadence changed"
area_tail = "    // Kept independent of world objects so boundary, expiry and cache limits can be tested."
assert drop_old[drop_old.index(area_tail):] == drop_new[drop_new.index(area_tail):], "Area windows or cap changed"
for token in ("WINDOWS.remove(level)",
              "ServerTickEvents.END_SERVER_TICK.register(server -> onServerTick())",
              "ServerLevelEvents.UNLOAD.register((server, level) -> onLevelUnload(level))"):
    assert token in drop_new

entry = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text("utf-8")
for token in ("BirdScanBudget.registerCallbacks();", "BirdDroppingAreaLimiter.registerCallbacks();"):
    assert token in entry, "Missing Fabric lifecycle registration: " + token
print("RUNTIME BUDGETS OK: unchanged scan token quota, 2048-drop area cache and Fabric cleanup hooks.")
