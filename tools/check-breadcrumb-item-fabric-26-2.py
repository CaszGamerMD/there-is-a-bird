#!/usr/bin/env python3
"""Require breadcrumb throws and nine-column scatter to remain byte-identical after 26.2 API swaps."""

from pathlib import Path
import subprocess


FILE = "src/main/java/EdDYON/guaniao/content/feed/BreadcrumbItem.java"
original = subprocess.check_output(["git", "show", f"origin/main:{FILE}"], text=True).replace("\r\n", "\n")
current = Path(FILE).read_text(encoding="utf-8").replace("\r\n", "\n")


def body(source: str, signature: str) -> str:
    opening = source.index("{", source.index(signature))
    depth = 1
    for index in range(opening + 1, len(source)):
        depth += (source[index] == "{") - (source[index] == "}")
        if depth == 0:
            return source[opening + 1:index]
    raise AssertionError(f"Unbalanced method: {signature}")


replacements = (
    ("InteractionResultHolder.pass(stack)", "InteractionResult.PASS"),
    ("InteractionResultHolder.sidedSuccess(stack, level.isClientSide)", "level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME"),
    ("InteractionResult.sidedSuccess(level.isClientSide)", "level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME"),
    ("!level.isClientSide", "!level.isClientSide()"),
    ("level.random", "level.getRandom()"),
    ("new ItemStack(GuaniaoItems.BREADCRUMBS.get())", "GuaniaoItems.BREADCRUMBS.get()"),
)


def migrated(source: str) -> str:
    for old, new in replacements:
        source = source.replace(old, new)
    return source


methods = (
    ("public InteractionResultHolder<ItemStack> use(", "public InteractionResult use("),
    ("public InteractionResult useOn(", "public InteractionResult useOn("),
    ("private Vec3 resolveAirAimPoint(", "private Vec3 resolveAirAimPoint("),
    ("private Vec3 resolveClickAimPoint(", "private Vec3 resolveClickAimPoint("),
    ("private boolean tossBreadcrumbs(", "private boolean tossBreadcrumbs("),
    ("private BlockPos findScatterLanding(", "private BlockPos findScatterLanding("),
    ("private boolean canScatterAt(", "private boolean canScatterAt("),
    ("private boolean scatterBreadcrumbs(", "private boolean scatterBreadcrumbs("),
    ("private boolean addServing(", "private boolean addServing("),
    ("private Vec3 handStartPosition(", "private Vec3 handStartPosition("),
    ("private void spawnThrowTrail(", "private void spawnThrowTrail("),
)

for old_signature, new_signature in methods:
    assert migrated(body(original, old_signature)) == body(current, new_signature), \
        f"Breadcrumb throw or serving gameplay drifted in {new_signature}"

assert "private static final double THROW_DISTANCE = 5.5;" in original
assert "private static final double THROW_DISTANCE = 5.5;" in current
assert "InteractionResultHolder" not in current
assert "return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;" in current

print("PASS: all 11 authored breadcrumb item methods retain identical throw, scatter and serving logic")
print("PASS: original 5.5-block reach, 3x3 landing, expiry timer and sparrow alert preserved")
