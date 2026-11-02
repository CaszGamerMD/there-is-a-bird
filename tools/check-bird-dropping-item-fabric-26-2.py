#!/usr/bin/env python3
"""Guard authored dropping charge, trajectory, fertilization and consumption through 26.2 item APIs."""

from pathlib import Path
import subprocess


FILE = "src/main/java/EdDYON/guaniao/content/dropping/BirdDroppingItem.java"
original = subprocess.check_output(["git", "show", f"origin/main:{FILE}"], text=True).replace("\r\n", "\n")
current = Path(FILE).read_text(encoding="utf-8").replace("\r\n", "\n")


def method_body(source: str, signature: str) -> str:
    opening = source.index("{", source.index(signature))
    depth = 1
    for index in range(opening + 1, len(source)):
        if source[index] == "{":
            depth += 1
        if source[index] == "}":
            depth -= 1
            if depth == 0:
                return source[opening + 1:index]
    raise AssertionError(f"Unbalanced method: {signature}")


replacements = (
    ("return InteractionResultHolder.consume(stack);", "return InteractionResult.CONSUME;"),
    ("InteractionResult.sidedSuccess(level.isClientSide)", "level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME"),
    ("this.getUseDuration(stack) - timeLeft", "this.getUseDuration(stack, entity) - timeLeft"),
    ("if (!level.isClientSide && entity instanceof ServerPlayer player)", "if (entity instanceof ServerPlayer player)"),
    ("!level.isClientSide", "!level.isClientSide()"),
    ("level.isClientSide)", "level.isClientSide())"),
    ("level.isClientSide {", "level.isClientSide() {"),
    ("level.random", "level.getRandom()"),
    ("addCooldown(this, 10)", "addCooldown(stack, 10)"),
    ("UseAnim.BOW", "ItemUseAnimation.BOW"),
    ("super.inventoryTick(stack, level, entity, slotId, isSelected)", "super.inventoryTick(stack, level, entity, slot)"),
    ("crop.isValidBonemealTarget(level, pos, state, false)", "crop.isValidBonemealTarget(level, pos, state)"),
)


def converted(body: str) -> str:
    for before, after in replacements:
        body = body.replace(before, after)
    return body


methods = (
    ("public InteractionResultHolder<ItemStack> use(", "public InteractionResult use("),
    ("public InteractionResult useOn(", "public InteractionResult useOn("),
    ("public void releaseUsing(", "public void releaseUsing("),
    ("public int getUseDuration(", "public int getUseDuration("),
    ("public UseAnim getUseAnimation(", "public ItemUseAnimation getUseAnimation("),
    ("public void inventoryTick(", "public void inventoryTick("),
    ("private InteractionResult fertilizeAroundFarmland(", "private InteractionResult fertilizeAroundFarmland("),
    ("private static boolean isValidFertilizeTarget(", "private static boolean isValidFertilizeTarget("),
    ("private static float getThrowPower(", "private static float getThrowPower("),
)

for old_signature, new_signature in methods:
    before = converted(method_body(original, old_signature))
    after = method_body(current, new_signature)
    assert before == after, f"Authored dropping gameplay drifted in {new_signature}"

for old, new in (
    ("private static final int USE_DURATION_TICKS = 72000;", "private static final int USE_DURATION_TICKS = 72000;"),
    ("private static final int MIN_THROW_CHARGE_TICKS = 3;", "private static final int MIN_THROW_CHARGE_TICKS = 3;"),
    ("private static final float MIN_THROW_SPEED = 0.45F;", "private static final float MIN_THROW_SPEED = 0.45F;"),
    ("private static final float MAX_THROW_SPEED = 1.75F;", "private static final float MAX_THROW_SPEED = 1.75F;"),
    ('new ResourceLocation(GuaniaoMod.MOD_ID, "husbandry/evidence_collector")', 'Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "husbandry/evidence_collector")'),
):
    assert old in original, f"Original baseline changed: {old}"
    assert new in current, f"26.2 migration lost: {new}"

for marker in (
    "public InteractionResult use(Level level, Player player, InteractionHand hand)",
    "public int getUseDuration(ItemStack stack, LivingEntity user)",
    "public ItemUseAnimation getUseAnimation(ItemStack stack)",
    "public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity entity, EquipmentSlot slot)",
    "ItemUseAnimation.BOW",
):
    assert marker in current, f"Missing 26.2 item contract: {marker}"

print("PASS: charged throw and exact projectile speed, scatter, cooldown and stack use preserved")
print("PASS: 3x3 bonemeal probability, particles, sounds, and advancement preserved")
print("PASS: 26.2 use, animation, inventory tick and fertilization signatures")
