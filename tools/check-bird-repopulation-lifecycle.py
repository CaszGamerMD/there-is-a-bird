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
# The scheduler's only migration is access to Level's now-private random
# field. Compare the COMPLETE original scheduling body after that precise
# API substitution, keeping retry timing, scan cadence and caps untouched.
old_work = old[old.index(head):old.index(tail_old)]
new_work = new[new.index(head):new.index(tail_new)]
assert old_work.replace("level.random.", "level.getRandom().") == new_work, (
    "Repopulation interval, chance, scan or cap calculation changed"
)

# The ecosystem checks and height boundaries retain their original implementation;
# allow only the typed gamerule and renamed height helpers in Minecraft 26.2.
start = "    private static boolean isEnabled("
stable_old = old[old.index(start):].replace(
    "getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)",
    "getGameRules().get(GameRules.SPAWN_MOBS)"
).replace("getMinBuildHeight()", "getMinY()").replace("getMaxBuildHeight()", "getMaxY()")
assert stable_old == new[new.index(start):], (
    "Bird natural habitat, cap, player-distance or population logic changed"
)

# Forge's potential-spawn hooks and weighted-list representation do not
# exist in 26.2. Check both the authored intent and the explicit Fabric
# implementation without pretending the old Java body can remain identical.
spawn_start = "    static int trySpawnAt("
original_spawn = old[old.index(spawn_start):old.index(start)]
ported_spawn = new[new.index(spawn_start):new.index(start)]
for token in (
    "BirdConfigManager.allowsNaturalSpawning(species)",
    "isActiveTime(species, level.getDayTime())",
    "BirdSpawnConfigEvents.remainingCapacity(level, surface.getX(), surface.getZ(), species) > 0",
    "BirdPopulationTracker.speciesAt(level, surface.getX(), surface.getZ(), species)",
    "double occupancy = Math.min(1.0D, (double)count / BirdConfigManager.maxWildNearby(species));",
    "int maxGroup = Math.min(MAX_PACK_SIZE,",
    "int minGroup = Math.min(maxGroup,",
    "for (int attempt = 0; attempt < PACK_ATTEMPTS && spawned < target; ++attempt)",
    "BirdScanBudget.tryAcquireForSpecies(level, species)",
    "level.addFreshEntity(mob)",
    "mob.isMaxGroupSizeReached(spawned)",
):
    assert token in original_spawn and token in ported_spawn, token
assert original_spawn.count("ForgeEventFactory.getPotentialSpawns(") == 2
assert ported_spawn.count("BirdSpawnConfigEvents.adjustedPotentialSpawns(") == 2
for old_api, new_api in (
    ("WeightedRandomList.create(candidates).getRandom(level.random)",
     "WeightedList.of(candidates).getRandom(level.getRandom())"),
    ("ForgeEventFactory.checkSpawnPosition(mob, level, MobSpawnType.NATURAL)",
     "mob.checkSpawnRules(level, EntitySpawnReason.NATURAL) && mob.checkSpawnObstruction(level)"),
    ("ForgeEventFactory.getMaxSpawnPackSize(mob)", "mob.getMaxSpawnClusterSize()"),
    ("data.type.create(level)", "data.type().create(level, EntitySpawnReason.NATURAL)"),
):
    assert old_api in original_spawn, old_api
    assert new_api in ported_spawn, new_api
assert "net.minecraftforge" not in new

assert "ServerTickEvents.END_LEVEL_TICK.register(BirdRepopulationEvents::onLevelTick);" in new
assert "ServerLevelEvents.UNLOAD.register((server, level) -> onLevelUnload(level));" in new
assert "ATTEMPTS.remove(level);" in new
assert "@Mod.EventBusSubscriber" not in new and "TickEvent.LevelTickEvent" not in new
initializer = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text("utf-8")
assert initializer.count("BirdRepopulationEvents.registerCallbacks();") == 1
print("PASS: native Fabric end-level tick and unload keep original bird repopulation work identical")
