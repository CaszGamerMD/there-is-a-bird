#!/usr/bin/env python3
"""Ensure Hummingbird client loops keep Forge timing/selection under Fabric hooks."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
source = "src/main/java/EdDYON/guaniao/client/hummingbird/"
manager = (root / source / "HummingbirdSoundManager.java").read_text("utf-8")
original = subprocess.check_output(
    ["git", "show", "origin/main:" + source + "HummingbirdSoundManager.java"],
    cwd=root, text=True,
).replace("import javax.annotation.Nullable;", "import org.jetbrains.annotations.Nullable;")
assert manager == original, "Hummingbird audio selection, pacing or stopping changed"

events = (root / source / "HummingbirdSoundEvents.java").read_text("utf-8")
client = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricClientEntrypoint.java").read_text("utf-8")
for expected in (
    "ClientTickEvents.END_CLIENT_TICK.register(",
    "HummingbirdSoundManager.INSTANCE.tick()",
    "ClientPlayConnectionEvents.DISCONNECT.register(",
    "HummingbirdSoundManager.INSTANCE.clear()",
):
    assert expected in events, f"Missing Fabric Hummingbird sound hook: {expected}"
assert "HummingbirdSoundEvents.registerCallbacks();" in client
assert "net.minecraftforge" not in events and "@SubscribeEvent" not in events
assert "if (level != minecraft.level) clear();" in manager
assert "clock - loop.startedAt() >= 20 && !playback.isActive(sound)" in manager
print("HUMMINGBIRD SOUND OK: original loop priorities and stop timing, Fabric client tick/disconnect wiring.")
