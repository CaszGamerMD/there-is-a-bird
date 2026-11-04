#!/usr/bin/env python3
"""Validate the Fabric fish capture callback stays before destructive conversion."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
base = ROOT / "src/main/java/EdDYON/guaniao/content/bird/nightheron"
entity = (base / "NightHeronEntity.java").read_text("utf-8")
event = (base / "NightHeronFishCaptureEvent.java").read_text("utf-8")
assert "MinecraftForge.EVENT_BUS" not in entity
assert "net.minecraftforge.eventbus" not in event
assert "EventFactory.createArrayBacked" in event
assert "return false;" in event  # no listeners -> original uncancelled capture
assert "if (listener.cancelCapture(attempt)) return true;" in event
start = entity.index("boolean catchFish(AbstractFish fish, HeldFishPurpose purpose)")
method = entity[start:entity.index("private boolean canCaptureFish", start)]
assert method.index("NightHeronFishCaptureEvent.isCancelled(") < method.index("fish.discard()")
assert "|| !this.canCaptureFish(fish, purpose)" in method
assert "this.capturingFish = false" in method
assert "public AbstractFish getFish()" in event
assert "public HeldFishPurpose getPurpose()" in event
print("PASS: original capture veto, fish protection and reentry guard preserved")
