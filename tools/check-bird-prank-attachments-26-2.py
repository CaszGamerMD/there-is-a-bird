#!/usr/bin/env python3
"""Verify Fabric prank persistence keeps the original player-specific penalties."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
rel = "src/main/java/EdDYON/guaniao/content/dropping/BirdDroppingPrankHandler.java"
old = subprocess.check_output(["git", "show", "origin/main:" + rel], cwd=root, text=True)
new = (root / rel).read_text("utf-8")
attach = (root / "src/main/java/EdDYON/guaniao/content/dropping/PrankEntityAttachments.java").read_text("utf-8")
entry = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text("utf-8")
build = (root / "build.gradle").read_text("utf-8")

for name, path, codec in (
    ("VILLAGER_PLAYER_HITS", "villager_dropping_player_hits", "Codec.INT"),
    ("GOLEM_PLAYER_HITS", "golem_dropping_player_hits", "Codec.INT"),
    ("GOLEM_PLAYER_HIT_TIMES", "golem_dropping_player_hit_times", "Codec.LONG"),
):
    pattern = (r"AttachmentType<Map<String, (?:Integer|Long)>> " + name
               + r"\s*=\s*AttachmentRegistry.createPersistent\(\s*"
               + r'Identifier.fromNamespaceAndPath\("guaniao", "' + path + r'"\),\s*'
               + r"Codec.unboundedMap\(Codec.STRING, " + codec + r"\)\)")
    assert re.search(pattern, attach), f"Missing persistent player history map {name}"

for name, path, codec in (
    ("VILLAGER_TRADE_PENALTY", "villager_dropping_trade_penalty", "INT"),
    ("VILLAGER_APPLIED_PENALTY", "villager_dropping_applied_penalty", "INT"),
    ("VILLAGER_TRADE_PENALTY_UNTIL", "villager_dropping_trade_penalty_until", "LONG"),
):
    assert re.search(r"AttachmentType<\w+> " + name + r"\s*=\s*AttachmentRegistry.createPersistent\(\s*"
                     + r'Identifier.fromNamespaceAndPath\("guaniao", "' + path + r'"\), Codec\.' + codec, attach)

assert "PrankEntityAttachments.registerAttachments();" in entry
assert "include 'EdDYON/guaniao/content/dropping/PrankEntityAttachments.java'" in build
assert "GuaniaoPrankEntityAttachmentProof.java" in build
assert "getPersistentData()" not in new and "net.minecraftforge" not in new

numeric = re.compile(r"private static final (?:int|float) (\w+)\s*=\s*([^;]+);")
assert dict(numeric.findall(old)) == dict(numeric.findall(new)), "Original prank timing, chances or penalty limits changed"
for path in ("husbandry/sky_delivery", "husbandry/good_luck_i_guess",
             "husbandry/helmet_saved_the_day", "husbandry/not_very_civilized",
             "husbandry/cake_no_longer_cake"):
    assert path in old and 'Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "' + path + '")' in new

for token in (
    "serverPlayer.getUUID().toString()", "hits.getOrDefault(key, 0) + 1",
    "villager.setAttached(PrankEntityAttachments.VILLAGER_PLAYER_HITS, Map.copyOf(hits))",
    "if (count >= 3)", "Math.min(3, count + 1)",
    "now - hitTimes.getOrDefault(playerId, 0L) > GOLEM_MEMORY_TICKS",
    "golem.setAttached(PrankEntityAttachments.GOLEM_PLAYER_HITS, Map.copyOf(hits))",
    "golem.setAttached(PrankEntityAttachments.GOLEM_PLAYER_HIT_TIMES, Map.copyOf(hitTimes))",
    "makeGolemRetaliate(golem, serverPlayer)",
    "getAttached(PrankEntityAttachments.VILLAGER_APPLIED_PENALTY) == null",
    "trackVillager(villager)", "tickVillagerTradePenalty(villager)",
    "TRACKED_VILLAGERS.remove(level)", "NEXT_VILLAGER_EXPIRY.remove(level)",
    "Math.min(VILLAGER_TRADE_PENALTY_MAX, VILLAGER_TRADE_PENALTY_BASE + Math.max(0, hitCount - 1) * 2)",
    "villager.removeAttached(PrankEntityAttachments.VILLAGER_TRADE_PENALTY_UNTIL)",
    "villager.setAttached(PrankEntityAttachments.VILLAGER_APPLIED_PENALTY, 0)",
    "offer.addToSpecialPriceDiff(-removable)", "offer.addToSpecialPriceDiff(diff)",
    "MobEffects.SLOWNESS", "MobEffects.NAUSEA", "EquipmentSlot.HEAD",
):
    assert token in new, f"Prank reaction or trade penalty invariant lost: {token}"

print("PRANK ATTACHMENTS OK: 3 player history maps, 3 trade penalty fields, original limits and resets.")
