#!/usr/bin/env python3
"""Assert native Fabric death-loot interception preserves authored bird death behavior."""
import json
import subprocess
from pathlib import Path
root = Path(__file__).resolve().parents[1]
prefix = "src/main/java/EdDYON/guaniao/event/"
def source(path): return (root / path).read_text("utf-8")
def baseline(name): return subprocess.check_output(["git","show","origin/main:"+prefix+name],cwd=root,text=True)
old = baseline("BirdFeatherEvents.java")
new = source(prefix+"BirdFeatherEvents.java")
start = "public static Item randomFeatherFor"
old_helpers = old.split(start,1)[1].split("    /** Birds never drop items on death. */",1)[0]
new_helpers = new.split(start,1)[1].split("    /**\n     * Suppress all ordinary",1)[0]
if old_helpers.replace("bird.level().random","bird.level().getRandom()") != new_helpers:
    raise SystemExit("Feather pools, shearing colors, spawn motion or density limits changed")
if "BirdSpecies.from(bird) != null" not in new or "net.minecraftforge" in new:
    raise SystemExit("Bird-only death suppression is missing or Forge-dependent")
old_rewards = baseline("BirdMutationEvents.java")
new_rewards = source(prefix+"BirdMutationEvents.java")
for label in ("GOLDEN_PURE ? 3 : 1","0.5F *","count++","Items.GOLD_INGOT"):
    if label not in old_rewards or label not in new_rewards:
        raise SystemExit("Rare-gold mutation reward formula changed: "+label)
if "getItemEnchantmentLevel(looting, killer.getWeaponItem())" not in new_rewards:
    raise SystemExit("Looting must come from killer's weapon")
mixin = source("src/main/java/EdDYON/guaniao/mixin/BirdDeathDropsMixin.java")
for required in ("suppressDeathDrops(bird)", "dropMutationGold(bird, level, source)",
                 "this.dropExperience(level, source.getEntity())", "ci.cancel()", "require = 1"):
    if required not in mixin:
        raise SystemExit("Death suppression/mutation/XP parity not wired: "+required)
mixins = json.loads(source("src/main/resources/guaniao.mixins.json"))
if "BirdDeathDropsMixin" not in mixins["mixins"]:
    raise SystemExit("Missing registered BirdDeathDropsMixin")
print("BIRD DEATH PARITY OK: no normal loot; original feathers, XP and rare mutation gold preserved")
