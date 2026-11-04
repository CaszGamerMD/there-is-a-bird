#!/usr/bin/env python3
"""Guard against accidental gameplay changes during the Forge -> Fabric pressure-pulse port."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PATH = "src/main/java/EdDYON/guaniao/content/dropping/BirdDroppingPressurePlatePulse.java"
INIT = ROOT / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java"

def verify():
    original = subprocess.check_output(
        ["git", "show", f"origin/main:{PATH}"], cwd=ROOT, text=True)
    source = (ROOT / PATH).read_text(encoding="utf-8")
    before = original[original.index("    public static boolean tryTrigger("):
                      original.index("    @SubscribeEvent")]
    after = source[source.index("    public static boolean tryTrigger("):
                   source.index("    /** Fabric's per-level end tick")]
    expected = before.replace(
        "state.entityInside(level, pos, triggeringEntity);",
        "state.entityInside(level, pos, triggeringEntity, InsideBlockEffectApplier.NOOP, false);")
    assert after == expected, "Pulsing logic, expiry tracking or cadence changed"
    assert "net.minecraftforge" not in source and "@Mod.EventBusSubscriber" not in source
    assert "ServerTickEvents.END_LEVEL_TICK.register(BirdDroppingPressurePlatePulse::onLevelTick);" in source
    assert "ServerLevelEvents.UNLOAD.register((server, level) -> onLevelUnload(level));" in source
    assert "level.getGameTime() % 200L != 0L" in source
    assert "pulses.values().removeIf(expiry -> expiry <= now);" in source
    assert "ACTIVE.remove(level);" in source
    entry = INIT.read_text(encoding="utf-8")
    assert "BirdDroppingPressurePlatePulse.registerCallbacks();" in entry
    assert entry.count("BirdDroppingPressurePlatePulse.registerCallbacks();") == 1
    print("Bird-dropping pressure pulse: gameplay body matches Forge; Fabric callbacks wired.")

if __name__ == "__main__":
    verify()
