#!/usr/bin/env python3
"""Preserve authored shearing, flight lock and regrowth behavior through the Fabric 26.2 port."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
src = "src/main/java/EdDYON/guaniao/"
def read(path):
    return (root / (src + path)).read_text(encoding="utf-8")
def original(path):
    return subprocess.check_output(["git", "show", "origin/main:" + src + path], cwd=root, text=True)

shear = read("event/BirdShearEvents.java")
flight = read("content/bird/flight/BirdFlightLock.java")
old_shear = original("event/BirdShearEvents.java")
old_flight = original("content/bird/flight/BirdFlightLock.java")
bootstrap = read("fabric/GuaniaoFabricEntrypoint.java")

for token in (
        "REGROW_TICKS = 6000",
        "BirdFeatherEvents.randomShearFeatherFor(bird)",
        "BirdFeatherEvents.spawnFeather(bird, feather, 1)",
        "BirdFlightLock.disableFlight(bird, REGROW_TICKS)"):
    if token not in old_shear or token not in shear:
        raise SystemExit("Bird shearing logic changed: " + token)
for token in (
        "long elapsed = now - last",
        "elapsed >= 0L && elapsed < REGROW_TICKS",
        "player.isSpectator()", "ShearsItem"):
    if token not in old_shear or token not in shear:
        raise SystemExit("Bird shearing conditions changed: " + token)
if "long now = bird.level().getGameTime()" not in old_shear or "long now = level.getGameTime()" not in shear:
    raise SystemExit("Original world-time cooldown clock not preserved.")
if "stack.hurtAndBreak(1, player, hand)" not in shear:
    raise SystemExit("Shears no longer lose one durability when successfully used.")
for token in (
        "AttachmentRegistry.create", "builder -> builder.persistent(Codec.LONG)",
        "UseEntityCallback.EVENT.register", "bird.getAttached(LAST_SHEAR_TIME)",
        "bird.setAttached(LAST_SHEAR_TIME, now)", "return InteractionResult.SUCCESS_SERVER",
        "return InteractionResult.SUCCESS", "level.isClientSide()"):
    if token not in shear:
        raise SystemExit("Fabric 26.2 shearing registration, saving or interaction missing: " + token)
if "BirdShearEvents.registerCallbacks();" not in bootstrap:
    raise SystemExit("Shearing callback not registered at Fabric startup.")

for token in (
        "long now = bird.level().getGameTime()",
        "now + Math.max(0, durationTicks)", "keepGrounded(bird)",
        "bird.setNoGravity(false)",
        "bird.onGround() ? Math.min(movement.y, 0.0D) : Math.min(movement.y, -0.08D)",
        "bird.setDeltaMovement(movement.x, vertical, movement.z)"):
    if token not in old_flight or token not in flight:
        raise SystemExit("Temporary flight-loss behavior changed: " + token)
if "bird.isInWaterOrBubble()" not in old_flight or (
        "bird.isInWater()" not in flight
        or "getBlockState(bird.blockPosition()).is(Blocks.BUBBLE_COLUMN)" not in flight):
    raise SystemExit("Original flight lock water/bubble-column exemption not preserved.")
for token in (
        "builder -> builder.persistent(Codec.LONG)",
        "bird.getAttachedOrElse(DISABLED_UNTIL, 0L)",
        "bird.setAttached(DISABLED_UNTIL, Math.max(",
        "bird.removeAttached(DISABLED_UNTIL)"):
    if token not in flight:
        raise SystemExit("Flight-loss attachment missing, no longer saved or stops expiring: " + token)
if "BirdFlightLock.registerAttachments();" not in bootstrap:
    raise SystemExit("Flight-loss attachment not initialized before birds load.")
for text in (shear, flight):
    if "net.minecraftforge" in text or "getPersistentData(" in text:
        raise SystemExit("Forge dependency remains in a migrated bird interaction.")
print("BIRD SHEARING PARITY OK: 6000-tick regrowth, mutation feathers, tool wear, persistent flight lock and server-only drops.")
