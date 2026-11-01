#!/usr/bin/env python3
"""Check the three original fan plumes' 26.2 datapack and custom-anvil contract."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "src/main/resources/data/guaniao"
NAMES = ("burial_plume", "riven_plume", "hunting_return")
IDS = {f"guaniao:{name}" for name in NAMES}

for name in NAMES:
    file = BASE / "enchantment" / f"{name}.json"
    definition = json.loads(file.read_text(encoding="utf-8"))
    assert definition == {
        "description": {"translate": f"enchantment.guaniao.{name}"},
        "supported_items": "guaniao:wind_feather_fan",
        "weight": 1,
        "max_level": 1,
        "min_cost": {"base": 1, "per_level_above_first": 0},
        "max_cost": {"base": 1, "per_level_above_first": 0},
        "anvil_cost": 8,
        "slots": ["mainhand"],
        "exclusive_set": "#guaniao:feather_fan_plumes",
    }, f"{file}: changed plume gameplay or Minecraft 26.2 codec fields"
    for language in ("en_us", "zh_cn"):
        translations = json.loads((ROOT / f"src/main/resources/assets/guaniao/lang/{language}.json").read_text(encoding="utf-8"))
        assert f"enchantment.guaniao.{name}" in translations
    assert not (ROOT / f"src/main/java/EdDYON/guaniao/content/enchantment/{''.join(p.title() for p in name.split('_'))}Enchantment.java").exists(), "legacy Forge Enchantment subclass still exists"

tag = json.loads((BASE / "tags/enchantment/feather_fan_plumes.json").read_text(encoding="utf-8"))
assert not tag.get("replace") and set(tag["values"]) == IDS and len(tag["values"]) == 3

src = ROOT / "src/main/java/EdDYON/guaniao"
registry = (src / "content/enchantment/GuaniaoEnchantments.java").read_text(encoding="utf-8")
assert "ResourceKey<Enchantment>" in registry and "getEnchantments().keySet()" in registry
assert "stack.enchant(registries.getOrThrow(key), 1)" in registry
assert "net.minecraftforge" not in registry

mixin = (src / "mixin/BurialPlumeAnvilMixin.java").read_text(encoding="utf-8")
assert '@Inject(method = "createResult", at = @At("RETURN"), require = 1)' in mixin
for original_rule in (
    "GuaniaoEnchantments.isWindFeatherFan(fan)",
    "GuaniaoEnchantments.forBook(book)",
    "GuaniaoEnchantments.hasAnyPlume(fan)",
    "this.repairItemCountCost = 1;",
    "this.cost.set(BurialPlumeAnvilEvents.ANVIL_LEVEL_COST)",
    "this.resultSlots.setItem(0, output)",
):
    assert original_rule in mixin, original_rule
assert "BurialPlumeAnvilMixin" in (ROOT / "src/main/resources/guaniao.mixins.json").read_text(encoding="utf-8")

for item in ("FeatherFanEnchantments.java",):
    assert "GuaniaoEnchantments.has(" in (src / "content/fan" / item).read_text(encoding="utf-8")

print("PASS: three data-driven 26.2 plumes, exclusive fan tags, 8-level one-book anvil and holder reads")
