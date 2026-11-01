#!/usr/bin/env python3
"""Guard the old hummingbird nectar, pollination and habitat contracts during the Fabric port."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PREFIX = "src/main/java/EdDYON/guaniao/"


def current(path):
    return (ROOT / (PREFIX + path)).read_text(encoding="utf-8")


def original(path):
    return subprocess.check_output(["git", "show", "origin/main:" + PREFIX + path], cwd=ROOT, text=True)


config = current("config/HummingbirdConfig.java")
old_config = original("config/HummingbirdConfig.java")
original_limits = dict(
    (name, (float(default), float(low), float(high)))
    for name, default, low, high in re.findall(
        r'b\.defineInRange\("(\w+)",\s*([\d.]+),\s*([\d.]+),\s*([\d.]+)\)', old_config
    )
)
new_defaults = dict((name, float(value)) for name, value in re.findall(
    r'^\s*(?:int|double)\s+(\w+)\s*=\s*([\d.]+);', config, re.MULTILINE
))
new_limits = dict((name, (float(low), float(high))) for name, low, high in re.findall(
    r'copy\.(\w+) = bounded\(input\.\1,\s*([\d.]+),\s*([\d.]+)\);', config
))
assert len(original_limits) == 16, f"expected all 16 numeric Forge settings, got {len(original_limits)}"
assert set(original_limits) == set(new_limits), "numeric config setting was removed"
for name, (default, low, high) in original_limits.items():
    assert new_defaults[name] == default, f"default changed for {name}"
    assert new_limits[name] == (low, high), f"bounds changed for {name}"
assert 'b.define("pollinationEnabled", true)' in old_config
assert 'boolean pollinationEnabled = true;' in config
for guard in ('Files.exists(PATH)', 'Files.writeString(PATH,', 'GSON.fromJson(',
              'settings = normalized(loaded);', 'JsonParser.parseReader(',
              'Failed to load hummingbird settings'):
    assert guard in config, f"Fabric config persistence or recovery missing: {guard}"
assert "ForgeConfigSpec" not in config

event = current("content/bird/hummingbird/HummingbirdPollinationEvent.java")
service = current("content/bird/hummingbird/HummingbirdPollinationService.java")
ecology = current("content/bird/hummingbird/HummingbirdEcologyEvents.java")
habitat = current("content/bird/hummingbird/FlowerHabitatIndex.java")
mixin = current("mixin/HummingbirdHabitatBlockUpdateMixin.java")
init = current("fabric/GuaniaoFabricEntrypoint.java")
mixins = (ROOT / "src/main/resources/guaniao.mixins.json").read_text(encoding="utf-8")

for source in (config, event, service, ecology, mixin):
    assert "net.minecraftforge" not in source, "unported Forge API in hummingbird chain"
for required in ('EventFactory.createArrayBacked', 'BeforeGrow', 'MobGriefing',
                 'getSources()', 'getOwners()', 'getTarget()', 'getRound()'):
    assert required in event, f"missing permission or source provenance contract: {required}"
for required in ('GameRules.MOB_GRIEFING', 'isValidBonemealTarget(level, pos, before)',
                 'MOB_GRIEFING.invoker().allow(', 'BEFORE_GROW.invoker().allow(',
                 'Objects.equals(source.owner(), currentOwner)', 'valid(level, active)',
                 'before.equals(FlowerHabitatIndex.readyState(level, pos))',
                 'crop.performBonemeal(', 'public static double chanceFor(int distinctSources)',
                 '0.25 * Math.min(4, Math.max(0, distinctSources))'):
    assert required in service, f"pollination safety/probability contract changed: {required}"
assert "ForgeEventFactory" not in service and "MinecraftForge" not in service
assert 'Identifier.fromNamespaceAndPath("guaniao", "hummingbird")' in service
for required in ('ServerTickEvents.END_LEVEL_TICK', 'ServerEntityEvents.ENTITY_UNLOAD',
                 'ServerChunkEvents.CHUNK_UNLOAD', 'ServerLevelEvents.UNLOAD',
                 'HummingbirdPollinationService.remove(mob)', 'FlowerHabitatIndex.releaseAll',
                 'FlowerHabitatIndex.forgetLevel', 'HummingbirdPollinationService.forgetLevel'):
    assert required in ecology, f"missing server lifecycle cleanup: {required}"
assert 'method = "setBlocksDirty"' in mixin
assert 'FlowerHabitatIndex.invalidate(level, pos)' in mixin
assert '"HummingbirdHabitatBlockUpdateMixin"' in mixins
assert "public static void invalidate(ServerLevel level, BlockPos pos)" in habitat
assert "HummingbirdConfig.loadOrCreateDefaults();" in init
assert "HummingbirdEcologyEvents.registerCallbacks();" in init
assert "FlowerHabitatIndex.tick(level);" in ecology and "HummingbirdPollinationService.tick(level);" in ecology

proof = (ROOT / "tools/port-probes/GuaniaoHummingbirdEcologyProof.java").read_text(encoding="utf-8")
assert "crop.isValidBonemealTarget(level, pos, state)" in proof
assert "level.setBlocksDirty(pos, state, state)" in proof
print("PASS: Fabric hummingbird settings, cancellable pollination, loaded-world cleanup, and habitat invalidation")
