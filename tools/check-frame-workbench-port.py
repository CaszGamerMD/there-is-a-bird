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

# Non-serialization substitutions are pinned: 26.2 makes the client flag
# private and drops SimpleContainer.addListener. Both slot callbacks must
# still bump the private work snapshot and mark the bench dirty.
original_ctor = "        Work(FrameWorkbenchBlockEntity bench) {\n            input.addListener(ignored -> { snapshotVersion++; bench.setChanged(); });\n            result.addListener(ignored -> { snapshotVersion++; bench.setChanged(); });\n        }"
modern_ctor = "        Work(FrameWorkbenchBlockEntity bench) {\n            // Minecraft 26.2 SimpleContainer no longer accepts change listeners.\n            // The override fires on the same setChanged() calls that notified\n            // our previous listener, without changing item flow or timing.\n            input = new SimpleContainer(1) {\n                @Override\n                public void setChanged() {\n                    super.setChanged();\n                    snapshotVersion++;\n                    bench.setChanged();\n                }\n            };\n            result = new SimpleContainer(1) {\n                @Override\n                public void setChanged() {\n                    super.setChanged();\n                    snapshotVersion++;\n                    bench.setChanged();\n                }\n            };\n        }"
if original_ctor not in expected or modern_ctor not in migrated:
    raise SystemExit("Frame Workbench inventory change hooks diverged from compatibility patch")
expected = expected.replace(".isClientSide", ".isClientSide()")
expected = expected.replace(
    "        final SimpleContainer input = new SimpleContainer(1);\n"
    "        final SimpleContainer result = new SimpleContainer(1);",
    "        final SimpleContainer input;\n"
    "        final SimpleContainer result;")
expected = expected.replace(original_ctor, modern_ctor)
if modern_ctor.count("snapshotVersion++") != 2 or modern_ctor.count("bench.setChanged()") != 2:
    raise SystemExit("Frame Workbench lost input or result inventory listeners")

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
