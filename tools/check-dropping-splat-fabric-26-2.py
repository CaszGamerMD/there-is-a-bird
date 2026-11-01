#!/usr/bin/env python3
"""Guard the 26.2 splat damage, brush, placement and old-world save contracts."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
rel = "src/main/java/EdDYON/guaniao/content/dropping/BirdDroppingSplatEntity.java"
old = subprocess.check_output(["git", "show", "origin/main:" + rel], cwd=root, text=True)
new = (root / rel).read_text("utf-8")
for token in (
    "FADE_TICKS = 20 * 20",
    "RAIN_WASH_CHANCE_PER_TICK = 0.003F",
    "this.wasAnchorMovedByPiston(validAnchor)",
    "this.updateAttachedPosition(attached)",
    "BirdAmbientDropControl.applyDroppingLifetime(itemEntity)",
    "BirdAmbientDropControl.HARD_MAX_DROPPINGS_NEARBY",
    "BirdConfigManager.droppingLifetimeMinTicks()",
    "BirdConfigManager.droppingLifetimeMaxTicks()",
):
    assert token in old and token in new, f"Original splat physics/age invariant missing: {token}"
for key in ("Age", "MaxAge", "StripDropping", "SurfaceDirection", "AnchorPos",
            "AttachmentPart", "LocalDirection", "LocalX", "LocalY", "LocalZ", "AttachedEntity"):
    assert '"' + key + '"' in old and '"' + key + '"' in new, f"Lost old-world save key: {key}"
for token in (
    "public boolean hurtServer(ServerLevel level, @NotNull DamageSource source, float amount)",
    "public boolean hurtClient(DamageSource source)",
    "public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitLocation)",
    "brush.hurtAndBreak(1, player, hand)",
    "this.spawnAtLocation(serverLevel, stack)",
    "protected void addAdditionalSaveData(ValueOutput tag)",
    "protected void readAdditionalSaveData(ValueInput tag)",
    'tag.store("AttachedEntity", UUIDUtil.CODEC, this.attachedEntityUuid)',
    'tag.read("AttachedEntity", UUIDUtil.CODEC).orElse(null)',
):
    assert token in new, f"26.2 splat contract missing: {token}"
assert "NetworkHooks" not in new and "CompoundTag" not in new and "software.bernie" not in new
assert "GuaniaoDroppingSplatEntityProof.java" in (root / "build.gradle").read_text("utf-8")
print("DROPPING SPLAT OK: exact anchor, wash, piston, brush and old-world save contract on 26.2.")
