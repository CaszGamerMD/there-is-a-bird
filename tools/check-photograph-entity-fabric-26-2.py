#!/usr/bin/env python3
"""Guard the real photograph placement and frame math during 26.2 HangingEntity port."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
source_path = "src/main/java/EdDYON/guaniao/content/camera/PhotographEntity.java"
before = subprocess.check_output(["git", "show", "origin/main:" + source_path], cwd=root,
                                 text=True).replace("\r\n", "\n")
after = (root / source_path).read_text("utf-8").replace("\r\n", "\n")
proof = (root / "tools/port-probes/GuaniaoPhotographHangingProof.java").read_text("utf-8")
gradle = (root / "build.gradle").read_text("utf-8")


def body(java, signature):
    at = java.index(signature)
    start = java.index("{", at)
    depth = 0
    for index in range(start, len(java)):
        if java[index] == "{":
            depth += 1
        elif java[index] == "}":
            depth -= 1
            if depth == 0:
                return java[start:index + 1]
    raise AssertionError("unclosed Java method: " + signature)


def adapt(original):
    # These are API-only substitutions; authored mask, rectangle, support and
    # customization logic must compare byte-for-byte after normalization.
    normalized = re.sub(r"\.isClientSide\b(?!\s*\()", ".isClientSide()", original)
    return (normalized.replace("this.direction", "this.getDirection()")
            .replace("this.isAddedToWorld()", "this.isAddedToLevel()")
            .replace("ResourceLocation", "Identifier")
            .replace("ForgeRegistries.ITEMS.getKey(", "BuiltInRegistries.ITEM.getKey(")
            .replace("InteractionResult.sidedSuccess(this.level().isClientSide())",
                     "this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME"))


preserved = (
    "public int getWidth()", "public int getHeight()",
    "public static boolean canPlace(", "public ItemStack getPickResult()",
    "public boolean survives()", "private boolean designSurvives()",
    "private void recalculateDesignGeometry()", "private Vec3 localPoint(",
    "public List<AABB> getOccupiedBoxes()", "public ItemStack getItem()",
    "public void setItem(ItemStack stack)", "public PhotographFrameStyle getFrameStyle()",
    "public FrameDesign getFrameDesign()", "public int getRotation()",
    "public void setRotation(int rotation)", "private InteractionResult interactWithDesign(",
    "private boolean mayModify(", "private static boolean isFeather(",
    "public void playPlacementSound()",
)
for signature in preserved:
    assert body(after, signature) == adapt(body(before, signature)), signature + " gameplay changed"

required = (
    "super.defineSynchedData(builder);", "extends HangingEntity",
    "protected AABB calculateBoundingBox(BlockPos anchor, Direction facing)",
    "super.setDirection(direction);", "this.getDirection().get3DDataValue()",
    "getAddEntityPacket(ServerEntity serverEntity)",
    "protected void addAdditionalSaveData(@NotNull ValueOutput tag)",
    "protected void readAdditionalSaveData(@NotNull ValueInput tag)",
    'tag.store("Item", ItemStack.CODEC, this.getItem());',
    'tag.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY)',
    "public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, Vec3 hitLocation)",
    "public boolean hurtServer(ServerLevel level, @NotNull DamageSource source, float amount)",
    "public void dropItem(ServerLevel level, @Nullable Entity breaker)",
    "this.spawnAtLocation(level, drop);", "GameRules.ENTITY_DROPS",
    "this.level().isClientSide() && PhotographData.hasImage(this.getItem())",
    "new PhotographEntity(this.level(), this.pos, this.getDirection(), inserted)",
    "this.frameDesign.hasArea() && this.designSurvives()",
    "this.frameDesign == null ? List.of(this.getBoundingBox()) : this.occupiedBoxes",
)
for token in required:
    assert token in after, "missing modern API or authored photograph behavior: " + token

for forbidden in ("net.minecraftforge", "this.direction", "setEntityRepresentation(",
                  "isAddedToWorld()", "InteractionResult.sidedSuccess(", "public boolean hurt(",
                  "public void dropItem(@Nullable Entity", "getAddEntityPacket()"):
    assert forbidden not in after, "legacy photograph API remains: " + forbidden

assert "GuaniaoPhotographHangingProof.java" in gradle
assert "extends HangingEntity" in proof and "getAddEntityPacket(ServerEntity serverEntity)" in proof
print("PASS: 19 photograph placement, silhouette, frame-edit and drop routines unchanged")
print("PASS: 26.2 facing sync, spawning, typed persistence, client interaction and server damage")
print("PASS: focused Java 25 HangingEntity/ValueInput/ValueOutput API compile registered")
