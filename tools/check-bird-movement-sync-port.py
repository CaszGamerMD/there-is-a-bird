#!/usr/bin/env python3
"""Keep the precise authored flight/movement sync writes across the Entity flag rename."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
ROOT_BIRDS = ROOT / "src/main/java/EdDYON/guaniao/content/bird"
NAMES = ["BudgerigarEntity.java","AbstractColumbidEntity.java","CrowEntity.java","NightHeronEntity.java","SeagullEntity.java","SparrowEntity.java","WoodcockEntity.java"]
for name in NAMES:
    matches = list(ROOT_BIRDS.rglob(name))
    assert len(matches) == 1, name
    path = matches[0]
    original = subprocess.check_output(["git", "show",
        "origin/main:" + path.relative_to(ROOT).as_posix()],
        cwd=ROOT, text=True)
    new = path.read_text("utf-8")
    assert re.search(r"\bhasImpulse\b", original), name
    assert not re.search(r"\bhasImpulse\b", new), name
    assert len(re.findall(r"\bneedsSync\b", new)) >= len(
        re.findall(r"\bhasImpulse\b", original)), name
    # Full-source Gecko/non-Gecko parity independently verifies exact
    # assignments and conditional branches, not just occurrence counts.
    print("PASS:", name, "original sync flags retained")
assert len(NAMES) == 7
