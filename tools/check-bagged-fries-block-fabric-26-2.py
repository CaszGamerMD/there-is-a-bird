#!/usr/bin/env python3
"""Verify bagged-fries eating and laxative branches survived the 26.2 block hook migration."""

from pathlib import Path
import subprocess


FILE = "src/main/java/EdDYON/guaniao/content/food/BaggedFriesBlock.java"
original = subprocess.check_output(["git", "show", f"origin/main:{FILE}"], text=True).replace("\r\n", "\n")
current = Path(FILE).read_text(encoding="utf-8").replace("\r\n", "\n")


def method_body(source: str, signature: str) -> str:
    start = source.index(signature)
    opening = source.index("{", start)
    depth = 1
    for index in range(opening + 1, len(source)):
        if source[index] == "{":
            depth += 1
        elif source[index] == "}":
            depth -= 1
            if depth == 0:
                return source[opening + 1:index]
    raise AssertionError(f"Incomplete {signature}")


authored = method_body(original, "public InteractionResult use(")
ported = method_body(current, "private InteractionResult useFries(")
expected = authored.replace(
    "InteractionResult.sidedSuccess(level.isClientSide)",
    "level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME")
expected = expected.replace("level.isClientSide", "level.isClientSide()")
# The sided-result replacement above already called the boolean method.
expected = expected.replace("level.isClientSide()()", "level.isClientSide()")
expected = expected.replace("level.random", "level.getRandom()")
expected = expected.replace("MobEffects.CONFUSION", "MobEffects.NAUSEA")
expected = expected.replace("MobEffects.MOVEMENT_SLOWDOWN", "MobEffects.SLOWNESS")
assert ported == expected, "Original laxative, food, sound, particle, stat or removal logic changed"

for marker in (
    'IntegerProperty.create("fries", 0, TOTAL_FRIES)',
    "TOTAL_FRIES = 13",
    "Block.box(5.0D, 0.0D, 5.0D, 11.0D, 11.0D, 11.0D)",
    "EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING",
    "simpleCodec(BaggedFriesBlock::new)",
    "useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,",
    "useWithoutItem(BlockState state, Level level, BlockPos pos,",
    "return useFries(state, level, pos, player, hand)",
    "return useFries(state, level, pos, player, InteractionHand.MAIN_HAND)",
    "updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,",
):
    assert marker in current, f"Missing 26.2-compatible or authored block behavior: {marker}"

print("PASS: original 13-fries geometry, facing and horizontal state kept")
print("PASS: authored eating, laxative, particles and cleanup decisions unchanged")
print("PASS: 26.2 item/no-item interactions and block support callbacks registered")
