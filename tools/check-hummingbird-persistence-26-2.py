#!/usr/bin/env python3
"""The hummingbird's original world-save keys and taming veto survive typed 26.2 persistence."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PREFIX = "src/main/java/EdDYON/guaniao/"


def original(path):
    return subprocess.check_output(["git", "show", "origin/main:" + PREFIX + path], cwd=ROOT, text=True)


def current(path):
    return (ROOT / (PREFIX + path)).read_text(encoding="utf-8")


entity_file = "content/bird/hummingbird/HummingbirdEntity.java"
garden_file = "content/bird/hummingbird/HummingbirdGardenBehavior.java"
old_entity, entity = original(entity_file), current(entity_file)
old_garden, garden = original(garden_file), current(garden_file)
sites = current("content/bird/hummingbird/HummingbirdSites.java")
events = current("event/BirdTamePermissionEvents.java")


def old_save_keys(source):
    return set(re.findall(r'tag\.put(?:Int|Long|Boolean|UUID|String)?\("(\w+)"', source))


def new_save_keys(source):
    return set(re.findall(r'output\.(?:putInt|putLong|putBoolean|putString|store)\("(\w+)"', source))


for label, old, new in (("entity", old_entity, entity), ("garden", old_garden, garden)):
    assert old_save_keys(old) == new_save_keys(new), f"{label} save keys changed: {old_save_keys(old) ^ new_save_keys(new)}"
    assert "CompoundTag" not in new and "ResourceLocation" not in new
    assert "ValueOutput" in new and "ValueInput" in new

assert 'output.store("TamingCandidate", UUIDUtil.CODEC, tamingCandidate)' in entity
assert 'input.read("TamingCandidate", UUIDUtil.CODEC).orElse(null)' in entity
assert 'output.store("GardenItem", ItemStack.CODEC, getHeldGardenItem())' in entity
assert 'input.read("GardenItem", ItemStack.CODEC).orElse(ItemStack.EMPTY)' in entity
assert 'input.getBooleanOr("Male", isMale())' in entity
assert 'BirdCommandMode.byId(input.getIntOr("HummingbirdCommand", 0))' in entity
assert 'input.getIntOr("TamingProgress", 0)' in entity
assert 'HummingbirdConfig.tamingFeeds() - 1' in entity
assert 'behavior.save(output)' in entity and 'behavior.load(input)' in entity
assert 'Identifier.tryParse(input.getStringOr("GardenDimension", ""))' in garden
assert 'Identifier.tryParse(input.getStringOr("LastFlower", ""))' in garden
for key in ("Garden", "LinkedBath", "Perch"):
    assert f'input.getLong("{key}").map(BlockPos::of).orElse(null)' in garden
assert 'input.getIntOr("SeedCooldown", HummingbirdConfig.seedMinTicks())' in garden
assert 'state.getOffset(pos)' in sites and 'Identifier.fromNamespaceAndPath' in sites
assert 'net.minecraftforge' not in entity
assert 'EventFactory.createArrayBacked' in events
assert 'BEFORE_TAME.invoker().canTame(this, player)' in entity
assert 'tame(player); setBirdCommandMode(BirdCommandMode.FOLLOW)' in entity
assert 'tamingProgress = Math.max(0, HummingbirdConfig.tamingFeeds() - 1)' in entity
assert 'ForgeEventFactory' not in entity

proof = (ROOT / "tools/port-probes/GuaniaoHummingbirdPersistenceProof.java").read_text(encoding="utf-8")
for required in ('UUIDUtil.CODEC', 'ItemStack.CODEC', 'ValueInput', 'ValueOutput',
                 'BirdTamePermissionEvents.BEFORE_TAME.register'):
    assert required in proof
print("PASS: hummingbird old-world save keys, typed NBT codecs, carrying, owner taming and veto parity")
