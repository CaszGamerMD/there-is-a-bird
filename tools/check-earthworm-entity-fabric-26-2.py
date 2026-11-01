#!/usr/bin/env python3
"""Protect earthworm burrow/motion behavior through the 26.2 Entity API port."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
rel = "src/main/java/EdDYON/guaniao/content/earthworm/EarthwormEntity.java"
old = subprocess.check_output(["git", "show", "origin/main:" + rel], cwd=root, text=True)
new = (root / rel).read_text("utf-8")
assert "net.minecraftforge" not in new and "CompoundTag" not in new

def between(source, begin, end):
    return source[source.index(begin):source.index(end, source.index(begin))]

support = "    private boolean hasSupport(Vec3 point) {"
assert between(old, support, "    @Override\n    protected void defineSynchedData(") == between(
    new, support, "    @Override\n    protected void defineSynchedData("), "Crawler soil support footprint changed"
runtime_start = "        if (this.home == null) this.home = this.position();"
runtime_end = "    public float burrowProgress() {"
assert between(old, runtime_start, runtime_end) == between(new, runtime_start, runtime_end), (
    "Burrow timing, steering, motion, collision or home radius changed")

for token in (
    "this.burrowAfter = 900 + this.random.nextInt(901)",
    "int cap = natural ? 4 : 12", "!level.noCollision(worm) || !worm.hasSupport(position)",
    "EarthwormMotion.soilTicks(this.soilAge, soil)", "EarthwormMotion.BURROW_TICKS",
    "this.move(MoverType.SELF, this.getDeltaMovement())",
    "!player.getInventory().add(stack)", "player.drop(stack, false)",
    '"HomeX"', '"HomeY"', '"HomeZ"', '"SoilAge"', '"BurrowAfter"', '"Burrow"',
):
    assert token in old and token in new, f"Earthworm original invariant lost: {token}"

for token in (
    "protected void defineSynchedData(SynchedEntityData.Builder builder)",
    "builder.define(BURROW, 0)",
    "EntitySpawnReason.NATURAL : EntitySpawnReason.TRIGGERED",
    "level.getRandom().nextFloat() * 360.0F",
    "this.level().isClientSide()",
    "public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitLocation)",
    "protected void addAdditionalSaveData(ValueOutput tag)",
    "protected void readAdditionalSaveData(ValueInput tag)",
    'tag.getDouble("HomeX").isPresent()',
    'tag.getInt("BurrowAfter").isPresent()',
    'tag.getIntOr("SoilAge", 0)',
    "return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME",
):
    assert token in new, f"26.2 earthworm entity API migration incomplete: {token}"
assert "NetworkHooks.getEntitySpawningPacket" not in new and "getAddEntityPacket()" not in new
assert "public void lerpTo(" not in new, "Removed 26.2 entity interpolation hook remains"
build = (root / "build.gradle").read_text("utf-8")
assert "GuaniaoEarthwormEntityProof.java" in build
print("EARTHWORM ENTITY OK: identical soil/burrow motion, 26.2 save keys and vanilla spawn/interaction contracts.")
