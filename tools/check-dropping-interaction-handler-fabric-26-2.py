#!/usr/bin/env python3
"""Keep authored prank-food and seven mob reactions unchanged across 26.2 entity relocations."""

from pathlib import Path
import subprocess


FILE = "src/main/java/EdDYON/guaniao/content/dropping/BirdDroppingInteractionHandler.java"
original = subprocess.check_output(["git", "show", f"origin/main:{FILE}"], text=True).replace("\r\n", "\n")
current = Path(FILE).read_text(encoding="utf-8").replace("\r\n", "\n")

replacements = (
    ("import net.minecraft.world.entity.animal.Cat;", "import net.minecraft.world.entity.animal.feline.Cat;"),
    ("import net.minecraft.world.entity.animal.Fox;", "import net.minecraft.world.entity.animal.fox.Fox;"),
    ("import net.minecraft.world.entity.animal.Wolf;", "import net.minecraft.world.entity.animal.wolf.Wolf;"),
    ("import net.minecraft.world.entity.monster.AbstractSkeleton;", "import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;"),
    ("import net.minecraft.world.entity.monster.Spider;", "import net.minecraft.world.entity.monster.spider.Spider;"),
    ("import net.minecraft.world.entity.monster.Zombie;", "import net.minecraft.world.entity.monster.zombie.Zombie;"),
    ("import org.joml.Vector3f;", "import net.minecraft.util.ARGB;"),
    ("new DustParticleOptions(new Vector3f(0.25F, 0.16F, 0.06F), 0.92F)",
     "new DustParticleOptions(ARGB.colorFromFloat(1.0F, 0.25F, 0.16F, 0.06F), 0.92F)"),
    ("new DustParticleOptions(new Vector3f(0.18F, 0.42F, 0.08F), 0.78F)",
     "new DustParticleOptions(ARGB.colorFromFloat(1.0F, 0.18F, 0.42F, 0.08F), 0.78F)"),
    ("MobEffects.MOVEMENT_SLOWDOWN", "MobEffects.SLOWNESS"),
    ("MobEffects.MOVEMENT_SPEED", "MobEffects.SPEED"),
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
)

expected = original
for before, after in replacements:
    assert before in expected, f"Expected original mob behavior marker missing: {before}"
    expected = expected.replace(before, after)

assert current == expected, "Dropping mob, fox-held-item, enderman-teleport or prank-food behavior drifted"
assert "level.random" not in current
print("PASS: seven mob reactions, enderman teleport and seagull dog-bark all retain original values")
print("PASS: prank-food stacking, splat particle counts, colors and knockback preserved")
