#!/usr/bin/env python3
"""Baseline guard for the three migrated additive global loot modifiers."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
SRC = "src/main/java/EdDYON/guaniao/"
def original(file):
    return subprocess.check_output(["git", "show", "origin/main:"+SRC+file],
                                   cwd=ROOT, text=True)
def current(file): return (ROOT/(SRC+file)).read_text("utf-8")

books0 = original("content/enchantment/FeatherFanBookLootModifier.java")
books = current("content/enchantment/FeatherFanBookLootModifier.java")
worms0 = original("content/bird/woodcock/EarthwormLootModifier.java")
worms = current("content/bird/woodcock/EarthwormLootModifier.java")
note = current("content/note/BirdNoteLootModifier.java")
registry = current("registry/GuaniaoLootModifierSerializers.java")
for token in ("CHEST_CHANCE = 0.04F", "MONSTER_CHANCE = 0.0025F",
              "FeatherFanEnchantmentBooks.randomBook(context.getRandom())",
              "context.getRandom().nextFloat() < chance"):
    assert token in books0 and token in books, token
for token in ("state.is(BirdTags.WOODCOCK_FORAGE_GROUND)",
              "tool.getItem() instanceof ShovelItem",
              "instanceof Player player", "!player.isCreative() && !player.isSpectator()",
              "context.getRandom().nextFloat() < 0.20F",
              "new ItemStack(GuaniaoItems.EARTHWORM.get())"):
    assert token in worms0 and token in worms, token
for file in (books, worms, note):
    assert "LootTableEvents.MODIFY_DROPS.register" in file
    assert "net.minecraftforge" not in file
    assert "generatedLoot.add(" in file
for cls in ("BirdNoteLootModifier", "FeatherFanBookLootModifier", "EarthwormLootModifier"):
    assert cls + ".registerCallbacks();" in registry
assert "GuaniaoLootModifierSerializers.registerCallbacks();" in current(
    "fabric/GuaniaoFabricEntrypoint.java")
assert "net.minecraftforge" not in registry
assert "BirdNoteLootModifier.registerCallbacks();" not in current(
    "fabric/GuaniaoFabricEntrypoint.java") # never register twice
print("PASS: 4% chest, 0.25% monster, 20% soil worm and note loot stay additive")
