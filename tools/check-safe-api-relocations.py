#!/usr/bin/env python3
"""Verify low-risk 26.2 package relocations preserve original bird gameplay."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
changes = {
    "content/bath/BirdBathAttraction.java": (
        ("net.minecraft.world.level.chunk.ChunkStatus",
         "net.minecraft.world.level.chunk.status.ChunkStatus"),
    ),
    "content/bird/species/NightHeronProfile.java": (
        ("net.minecraft.world.entity.animal.AbstractFish",
         "net.minecraft.world.entity.animal.fish.AbstractFish"),
    ),
    "content/bird/nightheron/NightHeronForagingGoal.java": (
        ("net.minecraft.world.entity.animal.AbstractFish",
         "net.minecraft.world.entity.animal.fish.AbstractFish"),
    ),
}
for relative, replacements in changes.items():
    path = "src/main/java/EdDYON/guaniao/" + relative
    original = subprocess.check_output(["git", "show", "origin/main:" + path], cwd=root, text=True)
    actual = (root / path).read_text(encoding="utf-8")
    expected = original
    for old, new in replacements:
        if old not in expected:
            raise SystemExit(f"Original referenced API missing from {relative}: {old}")
        expected = expected.replace(old, new)
    if actual != expected:
        raise SystemExit(f"Unexpected gameplay difference beyond 26.2 import relocation: {relative}")
    print(f"JAVA API RELOCATION PARITY OK: {relative}")
