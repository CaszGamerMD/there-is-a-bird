#!/usr/bin/env python3
"""Assert the exact original sound/particle registrations are retained.

Compares the 1.20.1 baseline in origin/main against the Fabric-port source.
Run from a full checkout with origin/main available.
"""
import re
import subprocess
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
SOURCE = "src/main/java/EdDYON/guaniao/registry"
def baseline(file):
    proc = subprocess.run(["git", "show", f"origin/main:{SOURCE}/{file}"],
                          cwd=ROOT, capture_output=True, text=True, check=True)
    return proc.stdout
def local(file):
    return (ROOT / SOURCE / file).read_text(encoding="utf-8")
def compare(file, generic, pattern):
    old, new = baseline(file), local(file)
    fields = re.compile(r"public\s+static\s+final\s+(?:RegistryObject|Supplier)<" + generic + r">\s+(\w+)\s*=")
    original_fields, port_fields = set(fields.findall(old)), set(fields.findall(new))
    original_ids, port_ids = set(pattern.findall(old)), set(pattern.findall(new))
    if original_fields != port_fields:
        raise SystemExit(f"{file}: altered field names: missing={sorted(original_fields-port_fields)} extra={sorted(port_fields-original_fields)}")
    if original_ids != port_ids:
        raise SystemExit(f"{file}: altered resource IDs: missing={sorted(original_ids-port_ids)} extra={sorted(port_ids-original_ids)}")
    print(f"PARITY OK: {file}: {len(port_fields)} registration names, {len(port_ids)} literal IDs")
compare("GuaniaoSoundEvents.java", "SoundEvent",
        re.compile(r"(?:register|SOUND_EVENTS\.register|registerEvent)\(\s*\"([^\"]+)\""))
compare("GuaniaoParticleTypes.java", "SimpleParticleType",
        re.compile(r"(?:register|PARTICLE_TYPES\.register)\(\s*\"([^\"]+)\""))
changed = subprocess.run(["git", "diff", "--name-only", "origin/main", "HEAD", "--",
                          "src/main/resources/assets/guaniao"], cwd=ROOT,
                         capture_output=True, text=True, check=True).stdout.splitlines()
restricted = [name for name in changed if Path(name).suffix.lower() in (".png", ".ogg", ".geo", ".animation")]
if restricted:
    raise SystemExit("Restricted visual/audio assets were altered: " + ", ".join(restricted))
print("PARITY OK: bird textures, sound assets and models remain unchanged.")

# Preserve the direct declarations and register(...) IDs while porting the
# ordinary block, block-entity, and world-feature Forge registries.
def compare_deferred_registry(file):
    old, new = baseline(file), local(file)
    field_pattern = re.compile(
        r"public\s+static\s+final\s+(?:RegistryObject|FabricRegistryQueue\.Ref)<[^\n=]+>\s+(\w+)\s*="
    )
    original_fields = set(field_pattern.findall(old))
    port_fields = set(field_pattern.findall(new))
    if not original_fields or original_fields != port_fields:
        raise SystemExit(f"{file}: declared fields differ: {sorted(original_fields)} != {sorted(port_fields)}")
    ids_pattern = re.compile(r'\.register\(\s*"([^"]+)"')
    original_ids = set(ids_pattern.findall(old))
    port_ids = set(ids_pattern.findall(new))
    if original_ids != port_ids:
        raise SystemExit(f"{file}: direct registration IDs differ: {sorted(original_ids)} != {sorted(port_ids)}")
    print(f"PARITY OK: {file}: {len(original_fields)} declarations, {len(original_ids)} direct IDs")

for registry_file in ("GuaniaoBlocks.java", "GuaniaoBlockEntityTypes.java", "GuaniaoFeatures.java", "GuaniaoEntityTypes.java", "GuaniaoCreativeTabs.java", "GuaniaoItems.java", "GuaniaoRecipeSerializers.java"):
    compare_deferred_registry(registry_file)
tag_before = baseline("GuaniaoBlockTags.java")
tag_after = local("GuaniaoBlockTags.java")
if '"guaniao", "bird_perches"' not in tag_before or '"guaniao", "bird_perches"' not in tag_after:
    raise SystemExit("Bird perch tag namespace or path changed.")
print("PARITY OK: guaniao:bird_perches block tag ID.")

# Minecraft 26.2 requires resource-keyed constructors. Ensure every original
# block declaration has its proper key, including variant-based cages/baths.
blocks = local("GuaniaoBlocks.java")
if blocks.count(".setId(GuaniaoRegistryIds.block(") != 8:
    raise SystemExit("GuaniaoBlocks must key all 6 literal + 2 variant block factories.")
entities = local("GuaniaoEntityTypes.java")
if ".build(GuaniaoRegistryIds.entity(id))" not in entities:
    raise SystemExit("Bird entity type registration lacks 26.2 resource key.")
if any(re.search(pattern, entities) for pattern in (r"net\.minecraftforge", r"new ResourceLocation", r"RegistryObject")):
    raise SystemExit("Old Forge entity registration remains.")
print("PARITY OK: keyed 26.2 block and bird entity constructors.")

items = local("GuaniaoItems.java")
assert "jukeboxPlayable(" in items and '"music_disc_uwu_funk"' in items, "Music disc conversion missing"
assert "new FullColorSpawnEggItem" in items, "Painted spawn egg conversion missing"
assert "saturationModifier(0.35F)" in items, "Original fries saturation was not preserved"
assert "FEATHERS_BY_SPECIES" in items and "FabricRegistryQueue.Ref::get" in items, "Natural feather mapping broken"
if "ForgeSpawnEggItem" in items or "RecordItem" in items:
    raise SystemExit("Item registry still depends on Forge's spawn eggs or removed RecordItem.")
print("PARITY OK: item fields, direct IDs, food values, feather mapping and music disc conversion.")
