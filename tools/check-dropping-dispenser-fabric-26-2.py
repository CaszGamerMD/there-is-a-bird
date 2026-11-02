#!/usr/bin/env python3
"""Preserve dispenser-thrown bird dropping variants and flight parameters after 26.2 removal of the old base."""

from pathlib import Path
import subprocess


FILE = "src/main/java/EdDYON/guaniao/content/dropping/BirdDroppingDispenserBehavior.java"
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


assert method_body(original, "public static void registerAll()") == method_body(current, "public static void registerAll()")
assert method_body(original, "protected Projectile getProjectile(") == method_body(current, "private BirdDroppingProjectileEntity getProjectile(")
assert "protected float getPower() {\n        return 1.25F;\n    }" in original
for marker in (
    "extends DefaultDispenseItemBehavior",
    "protected ItemStack execute(BlockSource source, ItemStack stack)",
    "source.state().getValue(DispenserBlock.FACING)",
    "DispenserBlock.getDispensePosition(source)",
    "projectile.shoot(direction.getStepX(), direction.getStepY() + 0.1D,",
    "direction.getStepZ(), 1.25F, 6.0F)",
    "level.addFreshEntity(projectile)",
    "stack.shrink(1)",
):
    assert marker in current, f"Missing vanilla dispenser parity: {marker}"
assert "AbstractProjectileDispenseBehavior" not in current

print("PASS: all four dropping types and their projectile item visuals unchanged")
print("PASS: original 1.25 dispenser flight speed, scatter, launch and item consumption preserved")
