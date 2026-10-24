#!/usr/bin/env python3
"""Bird droppings are ticked explicitly; no Forge event subscription is required."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
rel = "src/main/java/EdDYON/guaniao/event/BirdDroppingEvents.java"
original = subprocess.check_output(
    ["git", "show", "origin/main:" + rel], cwd=root, text=True)
assert "@SubscribeEvent" not in original, "Legacy handler actually uses the event bus"
assert original.count('@Mod.EventBusSubscriber(modid = "guaniao")') == 1
expected = original.replace("import net.minecraftforge.fml.common.Mod;\n", "").replace(
    '@Mod.EventBusSubscriber(modid = "guaniao")\n', "")
current = (root / rel).read_text("utf-8")
assert current == expected, "Original dropping behavior altered"
assert "public static void tickBird(LivingEntity entity)" in current
server = (root / "src/main/java/EdDYON/guaniao/event/BirdServerTickManager.java").read_text("utf-8")
assert "BirdDroppingEvents.tickBird(bird);" in server
assert "ServerTickEvents.END_SERVER_TICK.register(BirdServerTickManager::onServerTick)" in server
print("PASS: explicit 26.2 server ticking preserves original bird-dropping gameplay")
