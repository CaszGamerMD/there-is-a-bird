#!/usr/bin/env python3
"""Source parity for the Forge -> Fabric prank-food callback migration."""
import json
import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[1]
prefix = "src/main/java/EdDYON/guaniao/"
def source(path): return (root / (prefix + path)).read_text("utf-8")
def baseline(path):
    return subprocess.check_output(["git", "show", "origin/main:" + prefix + path],
                                   cwd=root, text=True)
original = baseline("event/PrankFoodEvents.java")
server = source("event/PrankFoodEvents.java")
client = source("client/dropping/PrankFoodClientEvents.java")
finish = source("mixin/PrankFoodFinishMixin.java")
data = source("content/dropping/PrankFoodUtil.java")
old_data = baseline("content/dropping/PrankFoodUtil.java")
for token in ("TAG_PRANK_FOOD", "TAG_ORIGINAL_ITEM", "TAG_DROPPING_VARIANT",
              "TAG_PRANK_NAME", "ZH_PHRASE_REPLACEMENTS", "EN_REPLACEMENTS"):
    assert token in old_data and token in data, token
for token in ("storedPrankDisplayName", "stableTooltip", "prank_food_suspicious",
              "dropping_evidence", "bird_dropping_fertilizer"):
    assert token in original and token in client, token
assert "UseItemCallback.EVENT.register" in server
assert "player.startUsingItem(hand)" in server
assert "InteractionResult.CONSUME" in server
assert "ItemTooltipCallback.EVENT.register" in client
assert "PrankFoodEffectUtil.apply(entity)" in finish
assert "active == entity.getItemInHand(entity.getUsedItemHand())" in finish
assert "net.minecraftforge" not in server + client + finish + data
assert "DataComponents.CUSTOM_DATA" in data
assert "CustomData.of(tag)" in data
assert "BuiltInRegistries.ITEM.getKey(food.getItem())" in data
assert "DataComponents.CUSTOM_NAME" in data
assert "PrankFoodEvents.registerCallbacks();" in source("fabric/GuaniaoFabricEntrypoint.java")
assert "PrankFoodClientEvents.registerCallbacks();" in source("fabric/GuaniaoFabricClientEntrypoint.java")
cfg = json.loads((root / "src/main/resources/guaniao.mixins.json").read_text("utf-8"))
assert "PrankFoodFinishMixin" in cfg["mixins"]
print("PRANK FOOD PARITY OK: original custom markers, consumption effect and tooltips are wired")
