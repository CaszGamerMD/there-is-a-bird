#!/usr/bin/env python3
"""Assert registry/data/tag migrations retain the original authored behavior."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
files = (
    "src/main/java/EdDYON/guaniao/content/bird/BirdTags.java",
    "src/main/java/EdDYON/guaniao/client/skybird/SkyBirdEcology.java",
    "src/main/java/EdDYON/guaniao/client/guide/HandbookData.java",
    "src/main/java/EdDYON/guaniao/content/cage/BirdCageVariant.java",
)
for path in files:
    original = subprocess.check_output(["git", "show", "origin/main:" + path], cwd=root, text=True)
    updated = (root / path).read_text(encoding="utf-8")
    expected = original.replace("ResourceLocation", "Identifier").replace(
        "new Identifier(", "Identifier.fromNamespaceAndPath(")
    if path.endswith("SkyBirdEcology.java"):
        # Modern world clocks replace pre-26.2 dimension day time.
        expected = expected.replace("level.getDayTime()", "level.getDefaultClockTime()")
    if updated != expected:
        raise SystemExit(f"Unexpected behavior or data migration outside identifier changes: {path}")
    print(f"IDENTIFIER PARITY OK: {path}")
