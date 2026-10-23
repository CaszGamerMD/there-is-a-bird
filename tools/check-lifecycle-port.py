#!/usr/bin/env python3
"""Source-level safeguards for behavior-preserving Forge -> Fabric lifecycle migrations.

These gates check original constants and wiring, not runtime behavioral parity.
A successful audit is no substitute for full client/server tests.
"""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
src = root / "src/main/java/EdDYON/guaniao"

def current(relative):
    return (src / relative).read_text("utf-8")

def original(relative):
    return subprocess.check_output(
        ["git", "show", f"origin/main:src/main/java/EdDYON/guaniao/{relative}"],
        cwd=root, text=True)

def assert_ported(relative):
    old, new = original(relative), current(relative)
    if not old:
        raise SystemExit(f"{relative}: original source unavailable")
    if re.search(r"net\.minecraftforge|\b@SubscribeEvent\b", new):
        raise SystemExit(f"{relative}: still references Forge events/APIs")
    return old, new

original_mod, new_mod = assert_ported("GuaniaoMod.java")
if 'MOD_ID = "guaniao"' not in new_mod or "LOGGER" not in new_mod:
    raise SystemExit("Mod identity or public logger was lost.")

_, config = assert_ported("config/BirdConfigManager.java")
if 'getConfigDir().resolve(GuaniaoMod.MOD_ID)' not in config:
    raise SystemExit("Bird settings must retain their original config/guaniao subdirectory.")
if 'bird_settings.json' not in config or 'loadForServer(' not in config:
    raise SystemExit("Bird settings file or world-scope loading was removed.")

_, cfg_events = assert_ported("event/BirdConfigLifecycleEvents.java")
for token in ("SERVER_STARTING", "SERVER_STOPPING", "loadForServer(server)", "loadOrCreateDefaults()", "registerCallbacks()"):
    if token not in cfg_events:
        raise SystemExit(f"Bird config lifecycle missing: {token}")

_, nests = assert_ported("event/CrowNestLifecycleEvents.java")
for token in ("ServerLevelEvents.UNLOAD.register", "clearLoadedNests(level)"):
    if token not in nests:
        raise SystemExit(f"Crow nest cleanup hook missing: {token}")

old_rain, rain = assert_ported("event/EarthwormRainEvents.java")
for token in (
        "now % 200L != 0L", "now + 800L", "level.random.nextFloat() >= 0.35F",
        "getBoolean(GameRules.RULE_DOMOBSPAWNING)", "BirdScanBudget.tryAcquire(level, 1)",
        "level.isRaining()", "Math.abs(surface.getY() - player.getY()) > 12.0D"):
    if token not in old_rain or token not in rain:
        raise SystemExit(f"Rain earthworm behavior changed/lost: {token}")
for token in ("ServerTickEvents.END_LEVEL_TICK.register", "ServerLevelEvents.UNLOAD.register", "COOLDOWNS.remove(level)"):
    if token not in rain:
        raise SystemExit(f"Rain earthworm lifecycle hook missing: {token}")

_, photo_legacy = assert_ported("event/PhotoLegacyMigrationEvents.java")
for token in (
        "ServerPlayConnectionEvents.JOIN.register",
        "ServerPlayConnectionEvents.DISCONNECT.register",
        "ServerTickEvents.START_SERVER_TICK.register",
        "onContainerOpen(player, opened)",
        "opened != previous", "opened != player.inventoryMenu",
        "LegacyPhotoMigration.queue(", "PREVIOUS_MENUS.clear()"):
    if token not in photo_legacy:
        raise SystemExit(f"Legacy photo container scanning missing: {token}")

_, runtime = assert_ported("event/BirdRuntimeConfigEvents.java")
for token in (
        "ServerPlayConnectionEvents.JOIN.register",
        "BirdConfigManager.birdsPassThroughLeaves()",
        "BirdConfigManager.aprilFoolsMode()",
        "BirdConfigManager.skyBirdEcologyEnabled()",
        "GuaniaoNetwork.sendToPlayer("):
    if token not in runtime:
        raise SystemExit(f"Bird runtime config sync missing: {token}")

for f in ("event/BirdServerTickManager.java", "event/PhotoTransferEvents.java"):
    _, text = assert_ported(f)
    if "registerCallbacks()" not in text:
        raise SystemExit(f"{f}: not ready for Fabric bootstrap")

boot = current("fabric/GuaniaoFabricEntrypoint.java")
queue_order = (
    "GuaniaoBlocks.BLOCKS.registerAll()",
    "GuaniaoBlockEntityTypes.BLOCK_ENTITY_TYPES.registerAll()",
    "GuaniaoEntityTypes.ENTITY_TYPES.registerAll()",
    "GuaniaoItems.ITEMS.registerAll()",
    "GuaniaoFeatures.FEATURES.registerAll()",
    "GuaniaoCreativeTabs.CREATIVE_MODE_TABS.registerAll()",
)
indices = [boot.find(text) for text in queue_order]
if min(indices) < 0 or indices != sorted(indices):
    raise SystemExit("Fabric registry dependency order was changed or a registry omitted.")

callbacks = (
    "BirdConfigLifecycleEvents", "BirdServerTickManager", "EarthwormRainEvents",
    "CrowNestLifecycleEvents", "PhotoLegacyMigrationEvents", "PhotoTransferEvents",
    "BirdRuntimeConfigEvents", "BirdHeadPerchEvents")
for callback in callbacks:
    if f"{callback}.registerCallbacks();" not in boot:
        raise SystemExit(f"Missing Fabric bootstrap event wiring: {callback}")
print("LIFECYCLE OK: original config paths, server lifecycle, worm parameters, photo tracking and registry ordering.")

old_head, head = assert_ported("event/BirdHeadPerchEvents.java")
for token in (
        "UseBlockCallback.EVENT.register",
        "ServerPlayConnectionEvents.DISCONNECT.register",
        "ServerLifecycleEvents.SERVER_STOPPING.register",
        "BirdHeadPlacement.tryPlace(owner, hand, hit)",
        "BirdHeadPlacement.isPlacementGesture(owner, hand)",
        "releaseHeadBirds(player)"):
    if token not in head:
        raise SystemExit(f"Head bird placement/release port missing: {token}")
