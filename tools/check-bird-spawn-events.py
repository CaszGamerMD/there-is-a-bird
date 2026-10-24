#!/usr/bin/env python3
"""Baseline checks for flyby flocks, colony caches and polluted cake lifecycle."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
srcdir = Path("src/main/java/EdDYON/guaniao/event")
def old(n):
    return subprocess.check_output(["git", "show", "origin/main:" + (srcdir / n).as_posix()],
                                   cwd=root, text=True)
def new(n):
    source = (root / srcdir / n).read_text("utf-8")
    if "net.minecraftforge" in source or "@SubscribeEvent" in source:
        raise SystemExit(f"Unported Forge listener in {n}")
    return source

old_fly, fly = old("BirdFlybySpawnEvents.java"), new("BirdFlybySpawnEvents.java")
constants = re.compile(r"private static final (?:double|int|float|long) ([A-Z_]+)\s*=\s*([^;]+);")
if constants.findall(old_fly) != constants.findall(fly):
    raise SystemExit("Original flyby spawn distance, sight or cooldown constants changed")
# Preserve all five habitat types and their exact spawn weights and nearby limits.
enum_start = "    private enum BirdKind {"
for token in (
        "NIGHT_HERON(NIGHT_HERON_HABITAT, 3, 8, 17, 20.0D, 8.0D, 4)",
        "SPARROW(SPARROW_HABITAT, 9, 4, 9, 15.0D, 8.0D, 24)",
        "BUDGERIGAR(BUDGERIGAR_HABITAT, 8, 6, 13, 17.0D, 8.0D, 22)",
        "SPOTTED_DOVE(SPOTTED_DOVE_HABITAT, 6, 10, 21, 21.0D, 10.0D, 14)",
        "PIGEON(PIGEON_HABITAT, 7, 11, 23, 22.0D, 11.0D, 16)",
        "random.nextInt(120)", "450 + random.nextInt(451)", "80 + random.nextInt(121)",
        "now - lastAttempt < 20L", "SeagullEntity.clearTheftTargetsFor(playerId)"):
    if token not in old_fly or token not in fly:
        raise SystemExit("Flyby bird selection, timing or logout safety changed: " + token)
for callback in ("ServerTickEvents.END_SERVER_TICK.register",
                 "ServerPlayConnectionEvents.DISCONNECT.register",
                 "ServerLevelEvents.UNLOAD.register"):
    if callback not in fly:
        raise SystemExit("Flyby spawn lifecycle missing: " + callback)
if "Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, id)" not in fly:
    raise SystemExit("Flyby habitat tag namespace changed")

colony_old, colony = old("BirdColonySpawnRules.java"), new("BirdColonySpawnRules.java")
for token in ("COLONY_CELL_SIZE = 64", "COLONY_CELL_DIVISOR = 12",
              "MAX_CACHED_CELLS_PER_LEVEL = 1024",
              "HABITAT_SCAN_BUDGET_COST = 2", "SCORE_SCAN_BUDGET_COST = 3"):
    if token not in colony_old or token not in colony:
        raise SystemExit("Colony scan budget or cell sizing changed: " + token)
if "ServerLevelEvents.UNLOAD.register" not in colony or "HABITAT_CACHE.remove(level)" not in colony:
    raise SystemExit("Colony-scanned habitat cache no longer clears on world unload")

cake_old, cake = old("PollutedCakeEvents.java"), new("PollutedCakeEvents.java")
for token in ("PollutedCakeData.get(level).tick(level)",
              "PollutedCakeData.get(level).validateChunk(",
              "PollutedCakeData.get(level).isPolluted(",
              "PrankFoodEffectUtil.apply(player)"):
    if token not in cake_old or token not in cake:
        raise SystemExit("Polluted cake original behavior lost: " + token)
for callback in ("ServerTickEvents.END_LEVEL_TICK.register",
                 "ServerChunkEvents.CHUNK_LOAD.register",
                 "UseBlockCallback.EVENT.register"):
    if callback not in cake:
        raise SystemExit("Polluted cake Fabric callback missing: " + callback)
if "return InteractionResult.PASS;" not in cake:
    raise SystemExit("Polluted cake listener must not suppress the original vanilla cake bite")

boot = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text("utf-8")
for name in ("BirdFlybySpawnEvents", "BirdColonySpawnRules", "PollutedCakeEvents"):
    if f"{name}.registerCallbacks();" not in boot:
        raise SystemExit(f"Missing Fabric bootstrap hook: {name}")
print("SPAWN/Cake OK: five flyby habitats, flock timing, colony budget, chunk cleanup and prank effect preserved.")
