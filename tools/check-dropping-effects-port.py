#!/usr/bin/env python3
"""Author-origin parity checks for 26.2 status effects and dust particles."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
BASE = "src/main/java/EdDYON/guaniao/content/dropping/"

OLD_EFFECTS = {
    "MobEffects.CONFUSION": "MobEffects.NAUSEA",
    "MobEffects.MOVEMENT_SLOWDOWN": "MobEffects.SLOWNESS",
    "MobEffects.MOVEMENT_SPEED": "MobEffects.SPEED",
}
DUST = {
    "new DustParticleOptions(new Vector3f(0.25F, 0.16F, 0.06F), 0.92F)":
        "new DustParticleOptions(ARGB.colorFromFloat(1.0F, 0.25F, 0.16F, 0.06F), 0.92F)",
    "new DustParticleOptions(new Vector3f(0.18F, 0.42F, 0.08F), 0.78F)":
        "new DustParticleOptions(ARGB.colorFromFloat(1.0F, 0.18F, 0.42F, 0.08F), 0.78F)",
    "new DustParticleOptions(new Vector3f(0.28F, 0.18F, 0.07F), 0.80F)":
        "new DustParticleOptions(ARGB.colorFromFloat(1.0F, 0.28F, 0.18F, 0.07F), 0.80F)",
    "new DustParticleOptions(new Vector3f(0.20F, 0.40F, 0.08F), 0.70F)":
        "new DustParticleOptions(ARGB.colorFromFloat(1.0F, 0.20F, 0.40F, 0.08F), 0.70F)",
}

def verify(filename):
    path = BASE + filename
    expected = subprocess.check_output(
        ["git", "show", f"main:{path}"], cwd=ROOT, text=True)
    migrated = (ROOT / path).read_text(encoding="utf-8")
    old_names = {key: expected.count(key) for key in OLD_EFFECTS}
    old_colors = {key: expected.count(key) for key in DUST}
    for before, after in OLD_EFFECTS.items():
        expected = expected.replace(before, after)
    if any(old_colors.values()):
        expected = expected.replace("import org.joml.Vector3f;", "import net.minecraft.util.ARGB;")
        for before, after in DUST.items():
            expected = expected.replace(before, after)
    assert migrated == expected, f"Unexpected gameplay, particle, sound or timing change in {filename}"
    assert not any(name in migrated for name in OLD_EFFECTS)
    assert "new Vector3f(" not in migrated
    print(f"{filename}: effect renames {old_names}; dust conversions {old_colors}; original behavior otherwise unchanged")

for file in ("PrankFoodEffectUtil.java", "BirdDroppingInteractionHandler.java", "PollutedCakeData.java"):
    verify(file)
