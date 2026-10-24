#!/usr/bin/env python3
"""Ensure packaged achievements preserve original rules with 26.2 display icons."""
from pathlib import Path
import re, json, subprocess

root = Path(__file__).resolve().parents[1]
prefix = "src/main/resources/data/guaniao/advancements/husbandry/"
originals = subprocess.check_output(
    ["git", "ls-tree", "-r", "--name-only", "origin/main", "--", prefix],
    cwd=root, text=True).splitlines()
packaged_dir = root / "build/resources/main/data/guaniao/advancement/husbandry"
icon_count = 0
for name in originals:
    original = subprocess.check_output(["git", "show", "origin/main:" + name], cwd=root)
    expected = re.sub(rb'("icon"\s*:\s*\{\s*)"item"(\s*:)',
                      rb'\g<1>"id"\g<2>', original, count=1)
    packaged = packaged_dir / Path(name).name
    if not packaged.exists() or packaged.read_bytes() != expected:
        raise SystemExit("Packaged achievement changed unexpectedly: " + name)
    icon = json.loads(expected).get("display", {}).get("icon")
    if icon:
        if "id" not in icon or "item" in icon:
            raise SystemExit("Invalid 26.2 achievement icon: " + name)
        icon_count += 1
if len(originals) != 24 or icon_count != 19:
    raise SystemExit(f"Unexpected advancement inventory: {len(originals)} files, {icon_count} icons")
print("26.2 ADVANCEMENT CODECS OK: 24 original achievements and 19 modern display icons")
