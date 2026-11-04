#!/usr/bin/env python3
"""Check 26.2 client item compatibility bridges without altering copyrighted assets."""
from pathlib import Path
import json
import re
import subprocess

root = Path(__file__).resolve().parents[1]
assets = root / "src/main/resources/assets/guaniao"
models = sorted((assets / "models/item").glob("*.json"))
client_dir = assets / "items"
if len(models) < 60:
    raise SystemExit("Missing original item models; refusing to validate an incomplete asset set.")
expected = {p.stem for p in models}
actual = {p.stem for p in client_dir.glob("*.json")}
if expected != actual:
    raise SystemExit(f"Missing client item definitions: {sorted(expected-actual)}; unexpected: {sorted(actual-expected)}")
for p in models:
    obj = json.loads((client_dir / p.name).read_text("utf-8"))
    model = obj.get("model", {})
    if model.get("type") != "minecraft:model" or model.get("model") != f"guaniao:item/{p.stem}":
        raise SystemExit(f"Changed item model reference: {p.stem}")
    if p.stem not in ("cockatoo_spawn_egg", "kingfisher_spawn_egg") and model.get("tints"):
        raise SystemExit(f"Unexpected tint on pre-painted item: {p.stem}")

definitions = {
    "cockatoo_spawn_egg": "cockatoo/CockatooDefinition.java",
    "kingfisher_spawn_egg": "kingfisher/KingfisherDefinition.java",
}
for name, subpath in definitions.items():
    definition = (root / "src/main/java/EdDYON/guaniao/content/bird" / subpath).read_text("utf-8")
    colors = [
        int(re.search(r"SPAWN_EGG_" + key + r"_COLOR\s*=\s*(0x[0-9a-fA-F]+|\d+)", definition).group(1), 0)
        for key in ("BASE", "SPOT")
    ]
    model = json.loads((client_dir / (name + ".json")).read_text("utf-8"))["model"]
    tints = model.get("tints", [])
    if len(tints) != 2 or any(t.get("type") != "minecraft:constant" for t in tints):
        raise SystemExit(f"{name} lost one of its original two egg tints.")
    if [t["value"] for t in tints] != colors:
        raise SystemExit(f"{name} uses different egg colors than the original author.")
    print(f"PARITY OK: {name}: background {colors[0]}, spots {colors[1]}")

songpath = root / "src/main/resources/data/guaniao/jukebox_song/uwu_funk.json"
song = json.loads(songpath.read_text("utf-8"))
original_items = subprocess.check_output(
    ["git", "show", "origin/main:src/main/java/EdDYON/guaniao/registry/GuaniaoItems.java"],
    cwd=root, text=True)
legacy_record = re.search(
    r"new RecordItem\((\d+),\s*GuaniaoSoundEvents\.MUSIC_DISC_UWU_FUNK,"
    r"\s*new Item\.Properties\(\)\.stacksTo\(1\),\s*(\d+)\)",
    original_items)
if not legacy_record:
    raise SystemExit("Unable to confirm the original RecordItem comparator and length in ticks.")
comparator, length_ticks = map(int, legacy_record.groups())
if (song.get("sound_event") != "guaniao:music_disc.uwu_funk"
        or song.get("length_in_seconds") != length_ticks / 20.0
        or song.get("comparator_output") != comparator):
    raise SystemExit("UWU Funk disc sound, duration, or redstone output differs from the original.")
if song.get("description", {}).get("translate") != "item.guaniao.music_disc_uwu_funk.desc":
    raise SystemExit("Music disc translated title was modified.")
print(f"PARITY OK: {len(models)} item models bridged and UWU Funk jukebox song metadata preserved.")
