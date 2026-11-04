#!/usr/bin/env python3
"""Guard the authored feather fan combat, hunting and first-person pose through the 26.2 port."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
ORIGINAL = "src/main/java/EdDYON/guaniao/content/fan/FeatherFanItem.java"


def source_from_main(path: str) -> str:
    return subprocess.check_output(["git", "show", f"origin/main:{path}"], cwd=ROOT, text=True)


def compact(value: str) -> str:
    return re.sub(r"\s+", "", value)


def method_body(code: str, signature: str) -> str:
    start = code.index(signature)
    opening = code.index("{", start)
    depth = 0
    for index in range(opening, len(code)):
        if code[index] == "{":
            depth += 1
        elif code[index] == "}":
            depth -= 1
            if depth == 0:
                return code[opening + 1:index]
    raise AssertionError(f"Unterminated method {signature}")


baseline = source_from_main(ORIGINAL)
fan = (ROOT / ORIGINAL).read_text()
pose = (ROOT / "src/main/java/EdDYON/guaniao/client/fan/FeatherFanHandPose.java").read_text()
mixin = (ROOT / "src/main/java/EdDYON/guaniao/mixin/FeatherFanHandPoseMixin.java").read_text()
mixin_config = (ROOT / "src/main/resources/guaniao.mixins.json").read_text()
assert "FeatherFanHandPoseMixin" in mixin_config
assert "submitArmWithItem" in mixin and "FeatherFanHandPose.apply" in mixin
assert "net.minecraftforge" not in fan + pose + mixin
assert "initializeClient" not in fan

constants = (
    "USE_DURATION_TICKS", "MIN_CHARGE_TICKS", "MAX_CHARGE_TICKS",
    "MIN_THROW_SPEED", "MAX_THROW_SPEED", "MELEE_KNOCKBACK",
    "HUNT_LOCK_DELAY_TICKS", "HUNT_LOCK_INTERVAL_TICKS", "HUNT_MAX_LOCKED_TARGETS",
    "HUNT_LOCK_RANGE", "HUNT_LOCK_MIN_DOT",
)
for name in constants:
    declaration = re.search(rf"private static final [^;]*\b{name}\s*=\s*[^;]+;", baseline)
    assert declaration, name
    assert compact(declaration.group()) in compact(fan), name

for damage, name in (("4.0D", "ATTACK_DAMAGE"), ("-2.1D", "ATTACK_SPEED")):
    assert f"Attributes.{name}" in fan and damage in fan
assert "ItemAttributeModifiers.builder()" in fan
assert "EquipmentSlotGroup.MAINHAND" in fan
assert "BASE_ATTACK_DAMAGE_ID" in fan and "BASE_ATTACK_SPEED_ID" in fan

for key in (
    "item.guaniao.wind_feather_fan.hunting",
    "item.guaniao.wind_feather_fan.riven",
    "item.guaniao.wind_feather_fan.burial",
    "item.guaniao.wind_feather_fan.",
    "ChatFormatting.GOLD", "ChatFormatting.BLUE", "ChatFormatting.DARK_AQUA",
    "ChatFormatting.DARK_GRAY", "ChatFormatting.ITALIC",
):
    assert key in baseline and key in fan, key

original_pose = method_body(baseline, "boolean applyForgeHandTransform(")
ported_pose = method_body(pose, "boolean apply(")
ported_pose = ported_pose.replace(
    "if (!(itemInHand.getItem() instanceof FeatherFanItem fan)) {\n            return false;\n        }", ""
).replace("fan.getUseDuration(itemInHand, player)", "FeatherFanItem.this.getUseDuration(itemInHand)")
ported_pose = ported_pose.replace("ItemStack.isSameItemSameComponents", "ItemStack.isSameItemSameTags")
assert compact(original_pose) == compact(ported_pose), "Charging pose mathematics changed"

for helper in ("getDampedPoseProgress", "smootherStep"):
    old = method_body(baseline, "private static float " + helper + "(")
    new = method_body(pose, "private static float " + helper + "(")
    assert compact(old) == compact(new), helper

for snippet in (
    "getHuntingLockCapacity(chargeTicks)",
    "isHuntingLockMilestone(chargeTicks)",
    "findHuntingTargets(player,",
    "getCharge(chargeTicks)",
    "FeatherFanProjectileEntity.HUNT_SPEED",
    "projectile.configurePiercing(thrownStack, hand)",
    "projectile.configureHunting(thrownStack, hand, charge, huntingTargets)",
    "projectile.configureThrow(thrownStack, hand, charge)",
    "player.getCooldowns().addCooldown(stack, 6)",
    "player.stopUsingItem()",
    "target.getType().builtInRegistryHolder().is(BirdTags.BIRDS)",
    "thrownStack.hurtAndBreak(1, player, hand)",
    "stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND)",
    "ItemUseAnimation.BLOCK",
):
    assert snippet in fan, snippet
assert "public boolean releaseUsing(" in fan
assert "public void hurtEnemy(" in fan
assert "public InteractionResult use(" in fan
assert "getUseDuration(ItemStack stack, LivingEntity user)" in fan
print("Feather fan 26.2 use/throw/combat/hunting and exact authored hand-pose parity: OK")
