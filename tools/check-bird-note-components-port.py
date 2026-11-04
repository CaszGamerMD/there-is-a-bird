#!/usr/bin/env python3
"""Source parity: written bird notes, authors and 26.2 reading rewards."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
base = "src/main/java/EdDYON/guaniao/"
def source(path):
    return (root / (base + path)).read_text("utf-8")
def previous(path):
    return subprocess.check_output(["git", "show", "origin/main:" + base + path],
                                   cwd=root, text=True)
original = previous("content/note/BirdNoteContent.java")
notes = source("content/note/BirdNoteContent.java")
pattern = r'new NoteTemplate\("[^"]+", "[^"]+", "[^"]+"\)'
assert re.findall(pattern, original) == re.findall(pattern, notes), "Any original note or author changed"
for token in ("NOTE_CHANCE = {0.20, 0.25, 0.30, 0.35}",
              "SPECIAL_BY_SECRET = {0.02, 0.12, 0.35, 0.75}",
              "MUTATION_WEIGHTS", "TIER_WEIGHTS", "DEV_NOTES", "SECRET = 3"):
    assert token in original and token in notes, token
assert "DataComponents.WRITTEN_BOOK_CONTENT" in notes
assert "Component.translatable(bodyKey)" in notes
assert "new WrittenBookContent(Filterable.passThrough(title), author, 0" in notes
assert "DataComponents.CUSTOM_DATA" in notes and "marker.putByte(NOTE_TAG, (byte) 1)" in notes
assert "book.setTag(tag)" not in notes
reading = source("event/BirdNoteEvents.java")
assert "UseItemCallback.EVENT.register" in reading
assert "stack.is(Items.WRITTEN_BOOK)" in reading
assert "DataComponents.WRITTEN_BOOK_CONTENT" in reading
assert "BirdAdvancements.grant(serverPlayer, BirdAdvancements.BIRD_NOTE)" in reading
assert "BirdAdvancements.awardDevNote(serverPlayer, content.author())" in reading
assert "return InteractionResult.PASS" in reading
adv = source("content/advancement/BirdAdvancements.java")
original_adv = previous("content/advancement/BirdAdvancements.java")
for person in ("蛋炒饭", "伊洛哥斯拉", "多雨", "老三", "千年村庄"):
    assert person in original_adv and person in adv
assert "AdvancementHolder" in adv and "Identifier.fromNamespaceAndPath" in adv
assert "BirdNoteEvents.registerCallbacks();" in source("fabric/GuaniaoFabricEntrypoint.java")
print("BIRD NOTE PARITY OK: original authored text keys, authors, distribution and rewards retained")
