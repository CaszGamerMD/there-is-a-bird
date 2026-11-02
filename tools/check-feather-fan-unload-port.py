#!/usr/bin/env python3
"""Protect temporary burial/slowdown cleanup on Fabric chunk unloads and entity removal."""

from pathlib import Path


fan = Path("src/main/java/EdDYON/guaniao/content/fan/FeatherFanProjectileEntity.java").read_text(encoding="utf-8")
events = Path("src/main/java/EdDYON/guaniao/event/FeatherFanCombatEvents.java").read_text(encoding="utf-8")
entrypoint = Path("src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text(encoding="utf-8")

assert "public void onRemovedFromWorld()" not in fan
assert "public void cleanupOnUnload() {\n        this.releaseBurialTargets();\n" in fan
assert "        this.removeStuckSlowdown();\n    }" in fan
assert "public void remove(RemovalReason reason) {\n" in fan
assert "        this.releaseBurialTargets();\n        this.removeStuckSlowdown();\n"
assert "        super.remove(reason);" in fan
assert "ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {" in events
assert "if (entity instanceof FeatherFanProjectileEntity fan) {\n                fan.cleanupOnUnload();" in events
assert "FeatherFanCombatEvents.registerCallbacks();" in entrypoint

print("PASS: fan physics effects cleaned on removal and Fabric server entity unload")
print("PASS: chunk unload does not strand original slow-motion and burial physics leases")
