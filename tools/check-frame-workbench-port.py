#!/usr/bin/env python3
"""Confirm private frame-workbench gameplay is unchanged by 26.2 persistence port."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
relative = "src/main/java/EdDYON/guaniao/content/camera/frame/FrameWorkbenchBlockEntity.java"
original = subprocess.check_output(["git", "show", "origin/main:" + relative], cwd=root, text=True)
migrated = (root / relative).read_text(encoding="utf-8")

expected = original.replace(
    "import net.minecraft.core.BlockPos;",
    "import net.minecraft.core.BlockPos;\nimport net.minecraft.core.HolderLookup;",
).replace(
    "import net.minecraft.nbt.CompoundTag;",
    "import net.minecraft.nbt.CompoundTag;\n"
    "import net.minecraft.world.level.storage.ValueInput;\n"
    "import net.minecraft.world.level.storage.ValueOutput;",
).replace("import net.minecraft.nbt.ListTag;\n", "").replace("import net.minecraft.nbt.Tag;\n", "")

start = "    // Chunk updates go to every observer."
end = "    static final class Work {"
for label, source in (("original", expected), ("port", migrated)):
    if start not in source or end not in source:
        raise SystemExit(f"Frame Workbench {label}: serialization section missing")
old_region = expected[expected.index(start):expected.index(end)]
new_region = migrated[migrated.index(start):migrated.index(end)]
expected = expected.replace(old_region, new_region, 1)
if expected != migrated:
    raise SystemExit("Frame Workbench changed outside the isolated persistence migration")

for field in ("Owner", "Input", "Result", "Pending", "ReservedSource",
              "Remaining", "Revision", "Seed", "Works"):
    if f'"{field}"' not in old_region or f'"{field}"' not in new_region:
        raise SystemExit(f"Frame Workbench lost original persisted field: {field}")

for invariant in (
    "getUpdateTag(HolderLookup.Provider registries)",
    "return new CompoundTag();",  # Private photos must never be visible in chunk updates.
    "protected void saveAdditional(ValueOutput output)",
    "protected void loadAdditional(ValueInput input)",
    'entry.putIntArray("Owner", uuidToIntArray(owner))',
    'entry.getIntArray("Owner")',
    'entry.store("Input", ItemStack.OPTIONAL_CODEC',
    'entry.read(name, ItemStack.OPTIONAL_CODEC)',
    "work.remaining = Math.max(0, Math.min(FrameWorkbenchMenu.CRAFT_DURATION_TICKS,",
    "if (work.pending.isEmpty() || work.remaining == 0 || !work.result.isEmpty())",
    "if (!stack.isEmpty()) stack.setCount(1);",
):
    if invariant not in new_region:
        raise SystemExit("Frame Workbench lost behavior/private-data invariant: " + invariant)
print("FRAME WORKBENCH PARITY OK: private work jobs and reserved refunds unchanged")
