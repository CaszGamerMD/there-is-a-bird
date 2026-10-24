#!/usr/bin/env python3
"""Verify every original Forge mod-bus registration has a Fabric counterpart."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
prefix = "src/main/java/EdDYON/guaniao/"
original_bus = subprocess.check_output(
    ["git", "show", "origin/main:" + prefix + "event/GuaniaoModEvents.java"],
    cwd=root, text=True)
original_droppings = subprocess.check_output(
    ["git", "show", "origin/main:" + prefix + "content/dropping/BirdDroppingUtil.java"],
    cwd=root, text=True)
def read(name):
    return (root / (prefix + name)).read_text("utf-8")

# Fail if the original subscriber later gains another event not accounted for.
handlers = re.findall(r"public static void (on[A-Z]\w+)\(", original_bus)
if handlers != [
    "onCommonSetup", "onEntityAttributeCreation",
    "onRegisterSpawnPlacements", "onBuildCreativeModeTabContents"
]:
    raise SystemExit(f"Unaccounted original Forge mod-bus handler: {handlers}")

retired = read("event/GuaniaoModEvents.java")
if "net.minecraftforge" in retired or "@SubscribeEvent" in retired:
    raise SystemExit("Retired Forge mod event subscriber still contains Forge APIs")

dropping = read("content/dropping/BirdDroppingUtil.java")
original_chances = re.findall(
    r"case (ONE|TWO|THREE|FOUR) -> ([0-9.]+)F;", original_droppings)
ported_chances = re.findall(
    r"case (ONE|TWO|THREE|FOUR) -> ([0-9.]+)F;", dropping)
if len(original_chances) < 8 or original_chances != ported_chances:
    raise SystemExit("Bird dropping compost or farmland fertilize chances changed")
for token in (
    "CompostableRegistry.INSTANCE.add(item, chance)",
    "GuaniaoItems.BIRD_DROPPING_1.get()",
    "GuaniaoItems.BIRD_DROPPING_2.get()",
    "GuaniaoItems.BIRD_DROPPING_3.get()",
    "GuaniaoItems.BIRD_DROPPING_4.get()",
):
    if token not in dropping:
        raise SystemExit("Bird dropping compostable registration lost: " + token)

entry = read("fabric/GuaniaoFabricEntrypoint.java")
for token in (
    "BirdDroppingUtil.registerCompostables();",
    "GuaniaoEntityAttributes.registerAll();",
    "GuaniaoVanillaCreativeTabs.registerCallbacks();",
    "GuaniaoEntityTypes.ENTITY_TYPES.registerAll();",
):
    if token not in entry:
        raise SystemExit(f"Missing Fabric setup equivalent to original Forge event: {token}")

print("SETUP PARITY OK: all original mod-event handlers replaced, four compost chances and farmland chances intact.")
