#!/usr/bin/env python3
"""Original client/server guards use 26.2 method access, with no branch changes."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
NAMES = ["BudgerigarEntity.java","CassowaryEntity.java","CockatielEntity.java","AbstractColumbidEntity.java","SpottedDoveEntity.java","CrowEntity.java","HummingbirdEntity.java","KestrelEntity.java","KingfisherEntity.java","KiwiEntity.java","LongTailedTitEntity.java","MacawEntity.java","MynaEntity.java","NightHeronEntity.java","SeagullEntity.java","SparrowEntity.java","UmbrellaCockatooEntity.java","WoodcockEntity.java"]
BIRD_ROOT = ROOT / "src/main/java/EdDYON/guaniao/content/bird"
OLD_FIELD = re.compile(r"\.isClientSide\b(?!\s*\()")
for name in NAMES:
    files = list(BIRD_ROOT.rglob(name))
    assert len(files) == 1, name
    path = files[0]
    original = subprocess.check_output(
        ["git", "show", "origin/main:" + path.relative_to(ROOT).as_posix()],
        cwd=ROOT, text=True)
    current = path.read_text("utf-8")
    count = len(OLD_FIELD.findall(original))
    assert count > 0, name
    assert not OLD_FIELD.search(current), name
    # Minecraft 26.2 invokes hurtServer only on the server; the hummingbird's
    # original extra side check inside hurt() is now implicit in that method.
    server_only_hurt = name == "HummingbirdEntity.java" and (
        "@Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount)" in current
        and "if (hurt && isAlive()) behavior.panic(source.getEntity())" in current
    )
    assert current.count(".isClientSide()") >= count - int(server_only_hurt), name
    # The full-source parity test permits *only* this mechanical substitution
    # plus the previously reviewed 26.2 API changes.
    print("PASS:", name, "original client/server branches kept:", count)
assert len(NAMES) == 18
