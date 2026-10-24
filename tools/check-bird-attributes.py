#!/usr/bin/env python3
"""Check every original bird default attribute provider survives the Fabric port."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
old = subprocess.check_output(
    ["git", "show", "origin/main:src/main/java/EdDYON/guaniao/event/GuaniaoModEvents.java"],
    cwd=root, text=True)
new = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoEntityAttributes.java").read_text("utf-8")

pattern_old = re.compile(r'event\.put\(\s*(?:\(EntityType\))?\s*GuaniaoEntityTypes\.(\w+)\.get\(\),\s*(\w+Entity)\.(\w+)\(\)\.build\(\)\s*\)')
pattern_new = re.compile(r'FabricDefaultAttributeRegistry\.register\(\s*GuaniaoEntityTypes\.(\w+)\.get\(\),\s*(\w+Entity)\.(\w+)\(\)\.build\(\)\s*\)')
before = pattern_old.findall(old)
after = pattern_new.findall(new)
if len(before) != 19:
    raise SystemExit(f"Original Forge source unexpectedly has {len(before)} bird attribute entries, not 19")
if before != after:
    raise SystemExit("Bird entity attribute profile or registration order was changed: "
                     f"missing={sorted(set(before)-set(after))}, extra={sorted(set(after)-set(before))}")
boot = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text("utf-8")
if not boot.index("GuaniaoEntityTypes.ENTITY_TYPES.registerAll();") < boot.index("GuaniaoEntityAttributes.registerAll();") < boot.index("GuaniaoItems.ITEMS.registerAll();"):
    raise SystemExit("Bird attributes must register after entity types and before item registry setup")
print("ATTRIBUTES OK: all 19 bird types retain their original exact provider methods and order.")
