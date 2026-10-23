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
