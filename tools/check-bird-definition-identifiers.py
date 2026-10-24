#!/usr/bin/env python3
"""Compare every changed bird definition to the original, allowing identifier substitutions only."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
definitions = (
    "budgerigar/BudgerigarDefinition",
    "cassowary/CassowaryDefinition",
    "cockatiel/CockatielDefinition",
    "cockatoo/CockatooDefinition",
    "crow/CrowDefinition",
    "kestrel/KestrelDefinition",
    "kiwi/KiwiDefinition",
    "kingfisher/KingfisherDefinition",
    "longtailedtit/LongTailedTitDefinition",
    "macaw/MacawDefinition",
    "myna/MynaDefinition",
    "seagull/SeagullDefinition",
    "sparrow/SparrowDefinition",
    "umbrellacockatoo/UmbrellaCockatooDefinition",
    "woodcock/WoodcockDefinition",
    "nightheron/NightHeronDefinition",
    "columbid/ColumbidDefinition",
)
for name in definitions:
    path = "src/main/java/EdDYON/guaniao/content/bird/" + name + ".java"
    original = subprocess.check_output(["git", "show", "origin/main:" + path], cwd=root, text=True)
    ported = (root / path).read_text(encoding="utf-8")
    expected = original.replace("ResourceLocation", "Identifier").replace(
        "new Identifier(", "Identifier.fromNamespaceAndPath(")
    if original == expected:
        raise SystemExit(f"Expected legacy identifier missing from original: {path}")
    if ported != expected:
        raise SystemExit(f"Bird gameplay/appearance constants changed beyond identifier substitution: {path}")
    print(f"BIRD DEFINITION PARITY OK: {name}")
print(f"Validated {len(definitions)} unchanged bird definitions.")
