#!/usr/bin/env python3
"""Guard original vanilla creative tab contents and ordering after Forge -> Fabric."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
source = subprocess.check_output(
    ["git", "show", "origin/main:src/main/java/EdDYON/guaniao/event/GuaniaoModEvents.java"],
    cwd=root, text=True)
port = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoVanillaCreativeTabs.java").read_text("utf-8")
old_eggs = re.findall(r"event\.accept\(\s*(?:\(ItemLike\))?GuaniaoItems\.(\w+_SPAWN_EGG)\.get\(\)\s*\)", source)
new_eggs = re.findall(r"entries\.accept\(GuaniaoItems\.(\w+_SPAWN_EGG)\.get\(\)\s*\)", port)
if len(old_eggs) != 19 or old_eggs != new_eggs:
    raise SystemExit(f"Bird egg vanilla tab changed: expected {old_eggs}, received {new_eggs}")
for x in (
    "CreativeModeTabs.INGREDIENTS",
    "CreativeModeTabs.SPAWN_EGGS",
    "entries.accept(GuaniaoItems.EARTHWORM.get())",
    "CreativeModeTabEvents.modifyOutputEvent(",
):
    if x not in port:
        raise SystemExit("Vanilla creative tab entry missing: " + x)
entry = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text("utf-8")
if entry.index("GuaniaoItems.ITEMS.registerAll();") > entry.index("GuaniaoVanillaCreativeTabs.registerCallbacks();"):
    raise SystemExit("Creative tab callbacks installed before bird items")
print("CREATIVE OK: earthworm in Ingredients; original 19 egg ordering retained in Spawn Eggs.")
