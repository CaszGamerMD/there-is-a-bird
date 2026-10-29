#!/usr/bin/env python3
"""Check chest-note loot injection and all authored distribution data remain intact."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
B = "src/main/java/EdDYON/guaniao/"
def before(path):
    return subprocess.check_output(["git", "show", "origin/main:"+B+path],
                                   cwd=ROOT, text=True).replace("\r\n","\n")
def now(path):
    return (ROOT/(B+path)).read_text("utf-8").replace("\r\n","\n")

old = before("content/note/BirdNoteContent.java")
new = now("content/note/BirdNoteContent.java")
for token in ("NOTE_CHANCE = {0.20, 0.25, 0.30, 0.35}", "TIER_WEIGHTS",
              "SPECIAL_BY_SECRET", "MUTATION_WEIGHTS", "MEDIUM_TABLES",
              "NETHER_TABLES", "DEV_NOTES"):
    assert token in old and token in new, token
matches = r'new NoteTemplate\("[^"]+", "[^"]+", "[^"]+"\)'
assert re.findall(matches, old) == re.findall(matches, new)
assert "roll(LootContext context, Identifier lootTableId)" in new
assert "tierIndex(lootTableId)" in new
assert "tierIndex(context.getQueriedLootTableId())" not in new

old_modifier = before("content/note/BirdNoteLootModifier.java")
new_modifier = now("content/note/BirdNoteLootModifier.java")
assert "BirdNoteContent.roll(context)" in old_modifier
assert "LootTableEvents.MODIFY_DROPS.register(" in new_modifier
assert "holder.unwrapKey().ifPresent(key ->" in new_modifier
assert "BirdNoteContent.roll(context, key.location())" in new_modifier
assert "generatedLoot.add(note)" in old_modifier and "generatedLoot.add(note)" in new_modifier
assert "net.minecraftforge" not in new_modifier
assert "BirdNoteLootModifier.registerCallbacks();" in now("fabric/GuaniaoFabricEntrypoint.java")
print("PASS: authored note rarity, authors, eligible chest keys and additive loot behavior retained")
