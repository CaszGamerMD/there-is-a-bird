#!/usr/bin/env python3
"""Protect droppings' 10-second lifetime and collision behavior through 26.2 APIs."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
rel = "src/main/java/EdDYON/guaniao/content/dropping/BirdDroppingProjectileEntity.java"
old = subprocess.check_output(["git", "show", "origin/main:" + rel], cwd=root, text=True)
# SynchedData builders and side guards were ported in earlier migration batches.
# Normalize those signatures before comparing the impact algorithm verbatim.
old = old.replace(".isClientSide", ".isClientSide()")
new = (root / rel).read_text("utf-8")

def section(source, first, last):
    start = source.index(first)
    return source[start:source.index(last, start)]

for first, last in (
    ("    public void tick() {", "    @Override\n    protected void onHitEntity"),
    ("    private boolean beginImpactResolution() {", "    private void splat() {"),
    ("    public BirdDroppingVariant getVariant() {", "    @Override\n    public void addAdditionalSaveData"),
):
    expected_end = last.replace("    public void addAdditionalSaveData", "    protected void addAdditionalSaveData")
    assert section(old, first, last) == section(new, first, expected_end), (
        "Projectile impacts, limits, variants, or visual strip trail changed near " + first)

old_hits = section(old, "    protected void onHitEntity(", "    /** Spawns the sticky splat")
new_hits = section(new, "    protected void onHitEntity(", "    /** Spawns the sticky splat")
assert old_hits.replace("MobEffects.MOVEMENT_SLOWDOWN", "MobEffects.SLOWNESS") == new_hits, (
    "Projectile hit/prank/slowness order changed")
for key in ("Variant", "NaturalDropping", "StripDropping", "StripStartedAt",
            "LaxativeDropping", "LifeTicks", "SourceBirdUuid"):
    assert '"' + key + '"' in old and '"' + key + '"' in new, (
        "Old-world projectile save key removed: " + key)

for token in (
    "MAX_FLIGHT_LIFETIME_TICKS = 20 * 10",
    "STRIP_STREAM_TICKS = 4",
    "if (BirdDroppingInteractionHandler.handleEntityHit(this, hit))",
    "BirdDroppingPrankHandler.handleCakeHit(this, result.getBlockPos())",
    "BirdDroppingPressurePlatePulse.tryTrigger(serverLevel, result.getBlockPos(), this)",
):
    assert token in old and token in new
for token in (
    "new ItemStack(variant == null ? BirdDroppingVariant.ONE.item() : variant.item())",
    "protected double getDefaultGravity()",
    "this.isStripDropping() ? 0.025D : 0.06D",
    "protected void addAdditionalSaveData(ValueOutput tag)",
    "protected void readAdditionalSaveData(ValueInput tag)",
    'tag.store("SourceBirdUuid", UUIDUtil.CODEC, this.sourceBirdUuid)',
    'tag.read("SourceBirdUuid", UUIDUtil.CODEC).orElse(null)',
    'tag.getLongOr("StripStartedAt",',
):
    assert token in new, f"26.2 droppings API missing: {token}"
assert "NetworkHooks" not in new and "software.bernie" not in new and "CompoundTag" not in new
assert "GuaniaoDroppingProjectileProof.java" in (root / "build.gradle").read_text("utf-8")
print("DROPPING PROJECTILE OK: identical impact, strip, save-key and lifetime behavior on 26.2.")
