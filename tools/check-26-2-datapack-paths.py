#!/usr/bin/env python3
"""Check that original advancements, loot tables and tags survive modern folder renames byte-for-byte."""
from pathlib import Path
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
        if new.read_bytes() != source:
            raise SystemExit(f"Authored datapack data was changed during path migration: {new_path}")
        checked += 1
        break
if checked != 98:
    raise SystemExit(f"Expected 98 relocated datapack files, found {checked}")
print(f"26.2 DATAPACK PATH PARITY OK: {checked} original advancements, loot tables, item/block/entity tags")
