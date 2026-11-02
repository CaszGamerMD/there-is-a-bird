#!/usr/bin/env python3
"""Byte-compare both authored tame paths after substituting the Fabric veto callback."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
prefix = "src/main/java/EdDYON/guaniao/content/bird/"
replacements = {
    "budgerigar/BudgerigarEntity.java": (
        "!net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)",
        "EdDYON.guaniao.event.BirdTamePermissionEvents.BEFORE_TAME.invoker().canTame(this, player)",
        "private void updateTrustedOwner(Player player)"),
    "umbrellacockatoo/UmbrellaCockatooEntity.java": (
        "net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)",
        "!EdDYON.guaniao.event.BirdTamePermissionEvents.BEFORE_TAME.invoker().canTame(this, player)",
        "private void completeTaming(Player player)"),
}


def method(java, signature):
    opening = java.index("{", java.index(signature))
    depth = 0
    for index in range(opening, len(java)):
        if java[index] == "{":
            depth += 1
        elif java[index] == "}":
            depth -= 1
            if depth == 0:
                return java[opening:index + 1]
    raise AssertionError("unclosed method: " + signature)


for path, (old_hook, new_hook, signature) in replacements.items():
    rel = prefix + path
    original = subprocess.check_output(["git", "show", "origin/main:" + rel], cwd=root,
                                       text=True).replace("\r\n", "\n")
    current = (root / rel).read_text("utf-8").replace("\r\n", "\n")
    assert original.count(old_hook) == 1, "original tame path changed: " + path
    assert method(current, signature) == method(original, signature).replace(old_hook, new_hook), \
        "authored tame threshold, command, or feedback changed: " + path
    assert "ForgeEventFactory" not in current
    print("PASS:", path, "original taming and rejection unchanged")

event = (root / "src/main/java/EdDYON/guaniao/event/BirdTamePermissionEvents.java").read_text("utf-8")
assert "EventFactory.createArrayBacked(Allow.class" in event
assert "if (!listener.canTame(bird, player)) return false;" in event
print("PASS: both tame attempts now call the existing cancellable Fabric event")
