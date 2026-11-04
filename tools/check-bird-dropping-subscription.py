#!/usr/bin/env python3
"""Bird droppings retain source gameplay and tick cadence with Fabric entity attachments."""
from pathlib import Path
import difflib
import re
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

# Ignore only the replaced Forge persistence block; the entire spawning,
# selection, cap and kingfisher animation logic must remain source-identical.
def without_persistence(source, first_line):
    source = source.replace("import net.minecraft.nbt.CompoundTag;\n", "")
    source = source.replace("import EdDYON.guaniao.content.bird.BirdEntityAttachments;\n", "")
    source = source.replace("import net.minecraft.world.level.block.Blocks;\n", "")
    source = source.replace("bird.isInWater()\n                || bird.level().getBlockState(bird.blockPosition()).is(Blocks.BUBBLE_COLUMN)",
                            "bird.isInWaterOrBubble()")
    source = re.sub(r'    private static final String TAG_COOLDOWN = "GuaniaoDroppingCooldown";\n', "", source)
    start = source.index(first_line)
    end = source.index("    private static boolean trySpawnDropping(", start)
    source = source[:start] + "        // cooldown stored per bird\n    }\n\n" + source[end:]
    source = source.replace("bird.getPersistentData().putInt(TAG_COOLDOWN, nextNaturalCooldown(bird));",
                            "// refresh cooldown")
    source = source.replace("bird.setAttached(BirdEntityAttachments.DROPPING_COOLDOWN, nextNaturalCooldown(bird));",
                            "// refresh cooldown")
    return source

actual_rest = without_persistence(current, "        Integer previousCooldown =")
expected_rest = without_persistence(expected, "        CompoundTag data =")
if actual_rest != expected_rest:
    print("".join(list(difflib.unified_diff(expected_rest.splitlines(True), actual_rest.splitlines(True)))[:40]))
    raise AssertionError("Bird dropping logic outside persistent cooldown changed")
for formula in (
    "INITIAL_JITTER_TICKS = 20 * 30", "CHECK_INTERVAL_TICKS = 20",
    "int cooldown = Math.max(0, previousCooldown - CHECK_INTERVAL_TICKS)",
    "nextNaturalCooldown(entity)", "Math.min(Integer.MAX_VALUE, initialCooldown)",
    "randomBetween(entity.getRandom(), RETRY_MIN_TICKS, RETRY_MAX_TICKS)",
    "kingfisher.tryStartDefecation() : trySpawnDropping(level, entity)",
    "bird.setAttached(BirdEntityAttachments.DROPPING_COOLDOWN, nextNaturalCooldown(bird))",
):
    assert formula in current, f"Dropping cooldown gameplay lost: {formula}"
assert "getPersistentData()" not in current
assert "public static void tickBird(LivingEntity entity)" in current
server = (root / "src/main/java/EdDYON/guaniao/event/BirdServerTickManager.java").read_text("utf-8")
assert "BirdDroppingEvents.tickBird(bird);" in server
assert "ServerTickEvents.END_SERVER_TICK.register(BirdServerTickManager::onServerTick)" in server
print("PASS: explicit Fabric ticking and persistent cooldown preserve original bird-dropping gameplay")
