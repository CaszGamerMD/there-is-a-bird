#!/usr/bin/env python3
"""Preserve original bird world-height boundaries through 26.2 accessor relocation."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
FILES = (
    "src/main/java/EdDYON/guaniao/content/bird/budgerigar/BudgerigarEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/columbid/AbstractColumbidEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/crow/CrowEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/kestrel/KestrelEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/nightheron/NightHeronEntity.java",
    "src/main/java/EdDYON/guaniao/event/BirdRepopulationEvents.java",
)
for rel in FILES:
    previous = subprocess.check_output(
        ["git", "show", "origin/main:" + rel], cwd=ROOT, text=True)
    current = (ROOT / rel).read_text("utf-8")
    for old, new in (("getMinBuildHeight()", "getMinY()"),
                     ("getMaxBuildHeight()", "getMaxY()")):
        assert old not in current, (rel, old)
        if old in previous:
            assert current.count(new) >= previous.count(old), (rel, old)
    # Independent GeckoLib/nullable/spawn parity gates compare all other source.
    print("PASS:", Path(rel).name, "uses native 26.2 world height bounds")
