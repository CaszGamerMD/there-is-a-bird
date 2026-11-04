#!/usr/bin/env python3
"""Verify 26.2 polluted cake saved data retains the authored long-array key and gameplay."""
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATH = "src/main/java/EdDYON/guaniao/content/dropping/PollutedCakeData.java"
source = (ROOT / PATH).read_text(encoding="utf-8")
original = subprocess.check_output(["git", "show", f"origin/main:{PATH}"], cwd=ROOT, text=True)

# The entire gameplay region including pollute(), 10-tick particle checks,
# 128-position/32-chunk budget and unload-sensitive iterator must stay identical
# to the Forge version, apart from the earlier packed-dust 26.2 API conversions.
def gameplay_body(s):
    return s[s.index("    public void pollute("):s.index("    @Override\n    public CompoundTag save(")]
original_gameplay = gameplay_body(original)
migrated_gameplay = source[source.index("    public void pollute("):source.index("    private void addPacked(")]
assert original_gameplay == migrated_gameplay, "Polluted cake runtime behavior changed"

# All original persistence keys, lookup, grouping, and dirty markers remain.
for invariant in (
    'private static final String DATA_NAME = "guaniao_polluted_cakes";',
    'private static final String TAG_POSITIONS = "Positions";',
    '.fieldOf(TAG_POSITIONS)',
    'Identifier.withDefaultNamespace(DATA_NAME)',
    'level.getDataStorage().computeIfAbsent(TYPE)',
    'savedPositions.forEach(packedPos -> data.addPacked(packedPos, false))',
    'this.positionsByChunk.values().stream()',
    '.flatMap(Set::stream).mapToLong(Long::longValue)',
    'this.setDirty();',
    'private void addPacked(long packedPos, boolean markDirty)',
):
    assert invariant in source, f"Missing persistence invariant: {invariant}"

assert 'Codec.LONG_STREAM' in source, "The codec must retain the NBT long-array encoding"
assert 'public CompoundTag save(' not in source, "Obsolete saved-data override remains"
assert 'computeIfAbsent(PollutedCakeData::load' not in source
assert source.count('Identifier.withDefaultNamespace(DATA_NAME)') == 1
print("PASS: polluted cake behavior matches main; Positions stays an NBT long array.")
