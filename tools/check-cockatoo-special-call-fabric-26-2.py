#!/usr/bin/env python3
"""Protect the one-recording-per-performance rule across Fabric sound hooks."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
base = "src/main/java/EdDYON/guaniao/client/entity/umbrellacockatoo/"
old = subprocess.check_output(["git", "show", "origin/main:" + base + "CockatooSpecialCallSoundEvents.java"], cwd=root, text=True)
new = (root / base / "CockatooSpecialCallSoundEvents.java").read_text("utf-8")
def between(source, start, end):
    return source[source.index(start):source.index(end, source.index(start))]

assert between(old, "        Call previous =", "    @SubscribeEvent public static void onClientTick") == between(
    new, "        Call previous =", "    private static void onClientTick"), "Cockatoo call-start and replay guards changed"
for token in (
    "previous.bird() == bird && previous.startedAt() == bird.getSpecialCallStartTick()",
    "bird.getSpecialCallElapsedTicks() > CockatooSpecialCall.START_WINDOW_TICKS",
    "CALLS.put(bird.getUUID(), new Call(bird, bird.getSpecialCallStartTick(), sound))",
    "Minecraft.getInstance().getSoundManager().play(sound)",
    "if (call.bird().level() != minecraft.level || !call.sound().canPlaySound())",
    "call.sound().finish()",
    "Minecraft.getInstance().getSoundManager().stop(call.sound())",
):
    assert token in old and token in new, f"Original cockatoo recording rule lost: {token}"

for hook in (
    "ClientTickEvents.END_WORLD_TICK.register(",
    "for (Entity entity : world.entitiesForRendering())",
    "ClientTickEvents.END_CLIENT_TICK.register(",
    "ClientPlayConnectionEvents.DISCONNECT.register(",
    "getSoundManager().isActive(call.sound())",
):
    assert hook in new, f"Fabric client lifecycle or engine-reload check missing: {hook}"
assert "net.minecraftforge" not in new and "@SubscribeEvent" not in new
entry = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricClientEntrypoint.java").read_text("utf-8")
assert "CockatooSpecialCallSoundEvents.registerCallbacks();" in entry
assert "GuaniaoCockatooSoundLifecycleProof.java" in (root / "build.gradle").read_text("utf-8")
print("COCKATOO CALL OK: original thirteen-second replay guards and Fabric world/tick/disconnect tracking.")
