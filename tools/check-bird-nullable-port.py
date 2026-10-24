#!/usr/bin/env python3
"""Preserve original nullable declarations when relocating the annotation import."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
FILES = (
    "src/main/java/EdDYON/guaniao/content/bird/cockatiel/CockatielEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/cockatoo/CockatooEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/hummingbird/HummingbirdEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/kestrel/KestrelEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/kingfisher/KingfisherEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/longtailedtit/LongTailedTitEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/macaw/MacawEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/myna/MynaEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/seagull/SeagullEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/umbrellacockatoo/UmbrellaCockatooEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/woodcock/WoodcockEntity.java",
)
for path in FILES:
    authored = subprocess.check_output(
        ["git", "show", "origin/main:" + path], cwd=ROOT, text=True)
    actual = (ROOT / path).read_text("utf-8")
    assert "import javax.annotation.Nullable;" in authored, path
    assert "import javax.annotation.Nullable;" not in actual, path
    assert actual.count("import org.jetbrains.annotations.Nullable;") == 1, path
    assert actual.count("@Nullable") == authored.count("@Nullable"), path
    # Whole-file GeckoLib/enum parity guards reject changes beyond expected
    # mechanical API adaptations (including this annotation package rename).
    print("PASS:", Path(path).name, "preserves original @Nullable declarations")
assert len(FILES) == 11
