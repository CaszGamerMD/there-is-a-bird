#!/usr/bin/env python3
"""Verify original resources survive singular 26.2 paths and icon codec changes only."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
original_paths = subprocess.check_output(
    ["git", "ls-tree", "-r", "--name-only", "origin/main", "--", "src/main/resources/data"],
    cwd=root, text=True,
).splitlines()
moves = (
    ("/data/guaniao/advancements/", "/data/guaniao/advancement/"),
    ("/data/guaniao/loot_tables/", "/data/guaniao/loot_table/"),
    ("/data/guaniao/tags/items/", "/data/guaniao/tags/item/"),
    ("/data/guaniao/tags/blocks/", "/data/guaniao/tags/block/"),
    ("/data/guaniao/tags/entity_types/", "/data/guaniao/tags/entity_type/"),
    ("/data/minecraft/tags/items/", "/data/minecraft/tags/item/"),
    ("/data/minecraft/tags/blocks/", "/data/minecraft/tags/block/"),
)
checked = 0
for original_path in original_paths:
    for previous, current in moves:
        if previous not in original_path:
            continue
        new_path = original_path.replace(previous, current, 1)
        old = root / original_path
        new = root / new_path
        if old.exists() or not new.is_file():
            raise SystemExit(f"Missing modern resource path or duplicate legacy path: {new_path}")
        source = subprocess.check_output(["git", "show", "origin/main:" + original_path], cwd=root)
        # Advancement display icons use ItemStackTemplate in 26.2, so a
        # targeted item -> id key migration is the only allowed data edit.
        # processResources also performs this normalization on the packaged
        # resource, including any still-authored 1.20-format advancement.
        allowed = source
        if "/advancements/husbandry/" in original_path:
            allowed = re.sub(rb'("icon"\s*:\s*\{\s*)"item"(\s*:)',
                             rb'\g<1>"id"\g<2>', source, count=1)
        if new.read_bytes() not in (source, allowed):
            raise SystemExit(f"Authored datapack data was changed outside the 26.2 icon conversion: {new_path}")
        checked += 1
        break
if checked != 98:
    raise SystemExit(f"Expected 98 relocated datapack files, found {checked}")
print(f"26.2 DATAPACK PATH PARITY OK: {checked} original advancements, loot tables, item/block/entity tags")
