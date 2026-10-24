#!/usr/bin/env python3
"""Check original bird flight interface semantics in native Minecraft 26.2."""
from pathlib import Path
import subprocess
import re

ROOT = Path(__file__).resolve().parents[1]
FILES = (
    "src/main/java/EdDYON/guaniao/content/bird/budgerigar/BudgerigarEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/columbid/AbstractColumbidEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/crow/CrowEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/hummingbird/HummingbirdEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/kestrel/KestrelEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/kingfisher/KingfisherEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/myna/MynaEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/seagull/SeagullEntity.java",
    "src/main/java/EdDYON/guaniao/content/bird/woodcock/WoodcockEntity.java",
)

for path in FILES:
    old = subprocess.check_output(
        ["git", "show", "origin/main:" + path], cwd=ROOT, text=True).replace("\r\n", "\n")
    current = (ROOT / path).read_text("utf-8").replace("\r\n", "\n")
    assert "import net.minecraft.world.entity.animal.FlyingAnimal;" in old
    assert "FlyingAnimal" not in current
    assert current.count("protected boolean omnidirectionalAirMover()") == 1
    assert re.search(r"@Override\s+protected boolean omnidirectionalAirMover\(\)\s*"
                     r"\{\s*return true;\s*\}", current)
    assert "public boolean isFlying()" in old and "public boolean isFlying()" in current
    # The GeckoLib bird source parity gate separately compares the complete
    # transformed source with the Forge originals, including every isFlying body.
    print("PASS:", Path(path).name, "uses the native air-movement marker")
assert len(FILES) == 9
