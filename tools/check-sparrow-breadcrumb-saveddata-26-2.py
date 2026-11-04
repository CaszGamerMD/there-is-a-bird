#!/usr/bin/env python3
"""Keep sparrow breadcrumb index NBT and claims identical after 26.2 SavedData port."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
rel = "src/main/java/EdDYON/guaniao/content/bird/sparrow/BreadcrumbSearchCache.java"
old = subprocess.check_output(["git", "show", "origin/main:" + rel], cwd=root, text=True)
new = (root / rel).read_text("utf-8")
assert "net.minecraftforge" not in new and "@SubscribeEvent" not in new

def between(source, begin, end):
    return source[source.index(begin):source.index(end, source.index(begin))]

assert between(old, "    public static List<BlockPos> nearby(", "    @SubscribeEvent") == between(
    new, "    public static List<BlockPos> nearby(", "    public static void onLevelUnload(")
assert between(old, "    private static final class TargetClaims {", "    private static final class BreadcrumbIndexData") == between(
    new, "    private static final class TargetClaims {", "    private static final class BreadcrumbIndexData"), "Claims TTL, cleanup or ownership changed"
index_region = "        private Set<Long> positionsInChunk(long chunkKey) {"
old_region = between(old, index_region, "        @Override\n        public CompoundTag save(")
new_region = new[new.index(index_region):new.rindex("    }\n}")]
assert old_region == new_region, "Chunk indexing, dirty markers or deduplication changed"
for token in (
    'private static final String DATA_NAME = "guaniao_breadcrumb_index"',
    'private static final String TAG_POSITIONS = "Positions"',
    "CLAIM_TTL_TICKS = 100L", "CLAIM_CLEANUP_INTERVAL_TICKS = 20L",
):
    assert token in old and token in new
for token in (
    "Codec.LONG_STREAM", ".fieldOf(TAG_POSITIONS)",
    "BreadcrumbIndexData::fromPositions", "BreadcrumbIndexData::positionsStream",
    "Identifier.withDefaultNamespace(DATA_NAME)", "level.getDataStorage().computeIfAbsent(TYPE)",
    "positions.forEach(packedPos -> data.addPacked(packedPos, false))",
    ".flatMap(Set::stream).mapToLong(Long::longValue)",
    "ServerLevelEvents.UNLOAD.register((server, level) -> onLevelUnload(level))",
    "TARGET_USERS.remove(level)",
):
    assert token in new, f"Breadcrumb save or unload invariant missing: {token}"
assert "CompoundTag save(" not in new and "computeIfAbsent(BreadcrumbIndexData::load" not in new
entry = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text("utf-8")
assert "BreadcrumbSearchCache.registerCallbacks();" in entry
print("BREADCRUMB SAVED DATA OK: original Positions long array, per-chunk index and claim TTL on Fabric 26.2.")
