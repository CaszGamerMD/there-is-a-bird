#!/usr/bin/env python3
"""The original 19 Forge biome spawn entries must survive the Fabric 26.2 port."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PREFIX = "src/main/java/EdDYON/guaniao/"
FILE = PREFIX + "world/GuaniaoBirdSpawnRegistry.java"
old = subprocess.check_output(["git", "show", "origin/main:" + FILE], cwd=ROOT, text=True)
new = (ROOT / FILE).read_text(encoding="utf-8")

def entries(source):
    return re.findall(r"new SpawnRule\(BirdSpecies\.([A-Z_]+),\s*GuaniaoEntityTypes\.([A-Z_]+),\s*(\d+)\)", source)

assert len(entries(old)) == len(entries(new)) == 19, "the original 19 species/weights have changed"
assert entries(old) == entries(new), "original species, entity types, order or base spawn weights changed"

for species, entity, _ in entries(new):
    assert species == entity, f"wrong entity for {species}"
    habitat = ROOT / f"src/main/resources/data/guaniao/tags/worldgen/biome/{species.lower()}_habitat.json"
    assert habitat.is_file(), f"missing original habitat tag for {species}"

for requirement in (
    "public static void registerCallbacks()",
    "BiomeModifications.addSpawn(",
    "BiomeSelectors.tag(rule.habitatTag)",
    "MobCategory.CREATURE",
    "rule.species.defaultMinGroup()",
    "rule.species.defaultMaxGroup()",
    "species.id() + \"_habitat\"",
):
    assert requirement in new, requirement
assert "net.minecraftforge" not in new and "ModifiableBiomeInfo" not in new

entrypoint = (ROOT / (PREFIX + "fabric/GuaniaoFabricEntrypoint.java")).read_text(encoding="utf-8")
assert "GuaniaoBirdSpawnRegistry.registerCallbacks();" in entrypoint
assert entrypoint.index("GuaniaoEntityTypes.ENTITY_TYPES.registerAll();") < entrypoint.index("GuaniaoBirdSpawnRegistry.registerCallbacks();")
for old_path in (
    PREFIX + "registry/GuaniaoBiomeModifierSerializers.java",
    PREFIX + "world/GuaniaoBirdSpawnBiomeModifier.java",
    "src/main/resources/data/guaniao/forge/biome_modifier/bird_spawns.json",
):
    assert not (ROOT / old_path).exists(), f"retired Forge biome modifier remains: {old_path}"

proof = ROOT / "tools/port-probes/GuaniaoBiomeSpawnProof.java"
assert "BiomeModifications.addSpawn(" in proof.read_text(encoding="utf-8")
print("PASS: all 19 authored bird biome spawns, habitat tags, weights and group bounds registered through Fabric 26.2")
