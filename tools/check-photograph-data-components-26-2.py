#!/usr/bin/env python3
"""Guard private photograph metadata semantics while replacing legacy ItemStack NBT."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
path = "src/main/java/EdDYON/guaniao/content/camera/PhotographData.java"
old = subprocess.check_output(["git", "show", "origin/main:" + path], cwd=root,
                              text=True).replace("\r\n", "\n")
new = (root / path).read_text("utf-8").replace("\r\n", "\n")
gradle = (root / "build.gradle").read_text("utf-8")

constants = r"public static final (?:int|String) [A-Z_]+ = [^;]+;"
assert re.findall(constants, old) == re.findall(constants, new), "photo ID or pixel constants changed"

for token in (
    "PhotoTransferLimits.isValidPhotoId(tag.getStringOr(TAG_PHOTO_ID, \"\"))",
    "PhotoTransferLimits.isValidPhotoId(id)",
    "PhotoImageCodec.isSha256(contentHash)",
    "width < MIN_IMAGE_SIZE", "height < MIN_IMAGE_SIZE",
    "width > MAX_IMAGE_SIZE", "height > MAX_IMAGE_SIZE",
    "tag.getIntArray(TAG_PIXELS).orElseGet(() -> new int[0])",
    "legacyPixels(tag).length == LEGACY_IMAGE_SIZE * LEGACY_IMAGE_SIZE",
    "target.putInt(TAG_WIDTH, width > 0 ? width : LEGACY_IMAGE_SIZE)",
    "target.putInt(TAG_HEIGHT, height > 0 ? height : LEGACY_IMAGE_SIZE)",
    "target.putIntArray(TAG_PIXELS, legacyPixels(source))",
    'tag.putString(TAG_CONTENT_HASH, contentHash)',
    "tag.remove(TAG_PIXELS)",
    "tag.store(TAG_PHOTOGRAPHER_ID, UUIDUtil.CODEC, photographerId)",
    "photoTag(stack).read(TAG_PHOTOGRAPHER_ID, UUIDUtil.CODEC).orElse(null)",
    "source.read(TAG_PHOTOGRAPHER_ID, UUIDUtil.CODEC)",
    "target.remove(TAG_PHOTOGRAPHER_ID)",
    "getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()",
    "CustomData.update(DataComponents.CUSTOM_DATA, stack, tag ->",
    "CustomData.update(DataComponents.CUSTOM_DATA, to, target ->",
):
    assert token in new, "missing original private photo behavior or 26.2 component API: " + token

for outdated in ("getOrCreateTag()", "getTag()", "putUUID(", "getUUID(",
                 "hasUUID(", "getInt(TAG_", "getLong(TAG_", "getString(TAG_",
                 "getIntArray(TAG_PIXELS).length"):
    assert outdated not in new, "legacy photo NBT accessor remains: " + outdated

original_tail = old.split("    private static boolean validDimensions(")[1]
ported_tail = new.split("    private static boolean validDimensions(")[1]
assert original_tail == ported_tail, "authored min/max photograph dimension rules changed"

for file in ("PhotographData.java", "PhotoTransferLimits.java", "PhotoImageCodec.java"):
    assert "include 'EdDYON/guaniao/content/camera/" + file + "'" in gradle

print("PASS: photo ID, photographer, UUID, content hash and timestamp keys retained")
print("PASS: original legacy pixel import and 16..1024 dimension limits preserved")
print("PASS: Minecraft 26.2 CustomData/UUID codec API included in real Java 25 compilation")
