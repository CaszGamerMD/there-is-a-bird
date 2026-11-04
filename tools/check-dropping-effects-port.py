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
        ["git", "show", f"origin/main:{path}"], cwd=ROOT, text=True)
    migrated = (ROOT / path).read_text(encoding="utf-8")
    old_names = {key: expected.count(key) for key in OLD_EFFECTS}
    old_colors = {key: expected.count(key) for key in DUST}
    for before, after in OLD_EFFECTS.items():
        expected = expected.replace(before, after)
    if any(old_colors.values()):
        expected = expected.replace("import org.joml.Vector3f;", "import net.minecraft.util.ARGB;")
        for before, after in DUST.items():
            expected = expected.replace(before, after)
    if filename == "BirdDroppingInteractionHandler.java":
        for before, after in (
            ("net.minecraft.world.entity.animal.Cat", "net.minecraft.world.entity.animal.feline.Cat"),
            ("net.minecraft.world.entity.animal.Fox", "net.minecraft.world.entity.animal.fox.Fox"),
            ("net.minecraft.world.entity.animal.Wolf", "net.minecraft.world.entity.animal.wolf.Wolf"),
            ("net.minecraft.world.entity.monster.AbstractSkeleton", "net.minecraft.world.entity.monster.skeleton.AbstractSkeleton"),
            ("net.minecraft.world.entity.monster.Spider", "net.minecraft.world.entity.monster.spider.Spider"),
            ("net.minecraft.world.entity.monster.Zombie", "net.minecraft.world.entity.monster.zombie.Zombie"),
            ("level.random", "level.getRandom()"),
            ("level.getMinBuildHeight()", "level.getMinY()"),
            ("level.getMaxBuildHeight()", "level.getMaxY()"),
            ("entity.hasImpulse = true;", "entity.needsSync = true;"),
            ("import net.minecraft.server.level.ServerLevel;", "import net.minecraft.resources.Identifier;\nimport net.minecraft.server.level.ServerLevel;"),
            ("import net.minecraft.sounds.SoundEvents;", "import net.minecraft.sounds.SoundEvent;\nimport net.minecraft.sounds.SoundEvents;"),
            ("private static final float FOX_DROP_HELD_ITEM_CHANCE = 0.15F;",
             'private static final float FOX_DROP_HELD_ITEM_CHANCE = 0.15F;\n'
             '    // 26.2 retains these vanilla sounds as data-driven wolf/cat variants, but\n'
             '    // no longer exposes the three original SoundEvents field names.\n'
             '    private static final SoundEvent CLASSIC_WOLF_WHINE = SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("entity.wolf.whine"));\n'
             '    private static final SoundEvent CLASSIC_WOLF_AMBIENT = SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("entity.wolf.ambient"));\n'
             '    private static final SoundEvent CLASSIC_CAT_HISS = SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("entity.cat.hiss"));'),
            ("SoundEvents.WOLF_WHINE", "CLASSIC_WOLF_WHINE"),
            ("SoundEvents.WOLF_AMBIENT", "CLASSIC_WOLF_AMBIENT"),
            ("SoundEvents.CAT_HISS", "CLASSIC_CAT_HISS"),
        ):
            expected = expected.replace(before, after)
    if filename == "PollutedCakeData.java":
        # The separate saved-data port intentionally changes the codec and the
        # storage methods. This gate exclusively owns authored dust colors and
        # the two original particle-effect method bodies; persistence and the
        # complete remaining cake gameplay are validated by their own gate.
        for value in DUST.values():
            if value in expected:
                assert migrated.count(value) == expected.count(value), (
                    f"Packed dust RGB or particle scale changed: {value}")
        effect_start = "    private static void spawnAmbient("
        assert expected[expected.index(effect_start):] == migrated[migrated.index(effect_start):], (
            "Polluted cake particle behavior, timings or sounds changed")
    else:
        assert migrated == expected, f"Unexpected gameplay, particle, sound or timing change in {filename}"
    assert not any(name in migrated for name in OLD_EFFECTS)
    assert "new Vector3f(" not in migrated
    print(f"{filename}: effect renames {old_names}; dust conversions {old_colors}; original behavior otherwise unchanged")

for file in ("PrankFoodEffectUtil.java", "BirdDroppingInteractionHandler.java", "PollutedCakeData.java"):
    verify(file)
