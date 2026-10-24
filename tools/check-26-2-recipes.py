#!/usr/bin/env python3
"""Check all original recipes survive 26.2's singular path and codec format."""
import json
import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[1]
old_root = "src/main/resources/data/guaniao/recipes/"
new_root = root / "src/main/resources/data/guaniao/recipe"
names = (
    "bagged_fries", "bird_bath", "bird_bath_2", "bird_dropping_food",
    "bird_guide", "breadcrumbs", "cooked_fries", "crow_nest",
    "film_to_photograph", "frame_workbench", "large_bird_cage",
    "medium_bird_cage", "nikon_d750", "raw_fries_horizontal",
    "raw_fries_vertical", "small_bird_cage", "stone_bird_bath",
    "stone_bird_bath_2", "wind_feather_fan", "wooden_bird_bath",
    "wooden_bird_bath_2",
)

def ingredient(value):
    if isinstance(value, list):
        return [ingredient(part) for part in value]
    if isinstance(value, dict):
        if len(value) == 1 and isinstance(value.get("item"), str):
            return value["item"]
        if len(value) == 1 and isinstance(value.get("tag"), str):
            return "#" + value["tag"]
    return value

def normalize(value):
    output = dict(value)
    kind = output["type"]
    if kind.startswith("minecraft:crafting_"):
        output["category"] = {
            "decorations": "building",
            "tools": "equipment",
            "food": "misc",
        }.get(output.get("category"), output.get("category"))
        if "key" in output:
            output["key"] = {symbol: ingredient(item) for symbol, item in output["key"].items()}
        if "ingredients" in output:
            output["ingredients"] = [ingredient(item) for item in output["ingredients"]]
    elif kind in ("minecraft:smelting", "minecraft:blasting",
                  "minecraft:smoking", "minecraft:campfire_cooking"):
        output["ingredient"] = ingredient(output["ingredient"])
    elif not kind.startswith("guaniao:"):
        raise SystemExit("Unexpected original recipe type: " + kind)
    result = output.get("result")
    if isinstance(result, dict) and "item" in result:
        output["result"] = {"id": result["item"], **{k: v for k, v in result.items() if k != "item"}}
    return output

assert len(names) == 21
actual_files = {p.stem for p in new_root.glob("*.json")}
if actual_files != set(names):
    raise SystemExit(f"Recipe file inventory changed: expected {set(names) - actual_files}; extra {actual_files - set(names)}")
for name in names:
    old_path = old_root + name + ".json"
    if (root / old_path).exists():
        raise SystemExit("Legacy datapack recipe path remains: " + old_path)
    old = json.loads(subprocess.check_output(["git", "show", "origin/main:" + old_path], cwd=root, text=True))
    new = json.loads((new_root / (name + ".json")).read_text("utf-8"))
    if new != normalize(old):
        raise SystemExit("Recipe changed outside the required 26.2 schema conversion: " + name)
    print("26.2 RECIPE PARITY OK:", name)
print("All 21 original recipes converted with ingredients, counts and outputs retained.")
