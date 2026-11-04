#!/usr/bin/env python3
"""Original bird synchronized fields, initial values and subclass order remain unchanged."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
NAMES = ["BudgerigarEntity.java","CassowaryEntity.java","CockatielEntity.java","AbstractColumbidEntity.java","PigeonEntity.java","SpottedDoveEntity.java","CrowEntity.java","HummingbirdEntity.java","KestrelEntity.java","KingfisherEntity.java","KiwiEntity.java","MynaEntity.java","NightHeronEntity.java","SeagullEntity.java","SparrowEntity.java","UmbrellaCockatooEntity.java","WoodcockEntity.java"]
BIRD_ROOT = ROOT / "src/main/java/EdDYON/guaniao/content/bird"

def method_body(src):
    match = re.search(r"protected void defineSynchedData\([^)]*\)\s*\{", src)
    assert match, "Missing synchronized-data method"
    start = match.end()
    nesting = 1
    for i in range(start, len(src)):
        if src[i] == "{": nesting += 1
        elif src[i] == "}":
            nesting -= 1
            if nesting == 0: return src[start:i]
    raise AssertionError("Unterminated synchronized-data method")

for name in NAMES:
    paths = list(BIRD_ROOT.rglob(name))
    assert len(paths) == 1, (name, paths)
    path = paths[0]
    old = subprocess.check_output(
        ["git", "show", "origin/main:" + path.relative_to(ROOT).as_posix()],
        cwd=ROOT, text=True).replace("\r\n", "\n")
    new = path.read_text("utf-8").replace("\r\n", "\n")
    authored = method_body(old)
    expected = authored.replace("super.defineSynchedData();", "super.defineSynchedData(builder);")
    expected = expected.replace("this.entityData.define(", "builder.define(")
    expected = expected.replace("entityData.define(", "builder.define(")
    assert method_body(new) == expected, f"{name}: original synced field order or default values changed"
    assert "protected void defineSynchedData(SynchedEntityData.Builder builder)" in new
    assert "super.defineSynchedData(builder);" in method_body(new)
    print("PASS:", name, "all original sync data defaults preserved")

assert len(NAMES) == 17
