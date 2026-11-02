#!/usr/bin/env python3
"""Byte-compare the original private frame-workbench/legacy-photo jobs after API-only rewrites."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
base = "src/main/java/EdDYON/guaniao/content/camera/"


def original(path):
    return subprocess.check_output(["git", "show", "origin/main:" + base + path],
                                   cwd=root, text=True).replace("\r\n", "\n")


def migrated(path):
    return (root / (base + path)).read_text("utf-8").replace("\r\n", "\n")


def replace_exact(source, before, after):
    assert before in source, "original migration site missing: " + before[:75]
    return source.replace(before, after)


legacy = original("LegacyPhotoMigration.java")
legacy = replace_exact(legacy, "import java.util.UUID;",
                       "import java.util.UUID;\nimport net.minecraft.core.component.DataComponents;")
legacy = replace_exact(legacy, "import net.minecraft.world.item.ItemStack;",
                       "import net.minecraft.world.item.ItemStack;\nimport net.minecraft.world.item.component.CustomData;")
legacy = replace_exact(legacy,
                       "task.stack().getOrCreateTag().putString(PhotographData.TAG_PHOTO_ID, result.photoId());",
                       "CustomData.update(DataComponents.CUSTOM_DATA, task.stack(),\n"
                       "                                tag -> tag.putString(PhotographData.TAG_PHOTO_ID, result.photoId()));")
assert legacy == migrated("LegacyPhotoMigration.java"), "legacy photo migration changed beyond the item-data port"

menu = original("frame/FrameWorkbenchMenu.java")
menu = replace_exact(menu, "import EdDYON.guaniao.network.GuaniaoNetwork;",
                     "import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;")
menu = replace_exact(menu, "import net.minecraft.core.BlockPos;",
                     "import net.minecraft.core.BlockPos;\nimport net.minecraft.core.component.DataComponents;\n"
                     "import net.minecraft.nbt.CompoundTag;")
menu = replace_exact(menu, "import net.minecraft.world.item.ItemStack;",
                     "import net.minecraft.world.item.ItemStack;\nimport net.minecraft.world.item.component.CustomData;")
menu = replace_exact(menu,
                     "    public FrameWorkbenchMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {\n"
                     "        this(containerId, inventory, buffer.readBlockPos(), null);\n    }",
                     "    public FrameWorkbenchMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {\n"
                     "        this(containerId, inventory, buffer.readBlockPos());\n    }\n\n"
                     "    /** Fabric extended-menu opening datum, with the same original block position. */\n"
                     "    public FrameWorkbenchMenu(int containerId, Inventory inventory, BlockPos pos) {\n"
                     "        this(containerId, inventory, pos, null);\n    }")
menu = re.sub(r"\.isClientSide\b(?!\s*\()", ".isClientSide()", menu)
menu = replace_exact(menu,
                     "GuaniaoNetwork.sendToPlayer(new FrameWorkSnapshotPacket(containerId, workbenchPos,\n"
                     "                    work.preview().copy(), work.seed, work.remaining, work.revision), player);",
                     "ServerPlayNetworking.send(player, new FrameWorkSnapshotPacket(containerId, workbenchPos,\n"
                     "                    work.preview().copy(), work.seed, work.remaining, work.revision));")
menu = replace_exact(menu,
                     "        ItemStack source = getPhotograph();\n"
                     "        if (!source.isEmpty() && (!isSource(source)\n"
                     "                || (source.hasTag() && source.getTag().contains(PhotographData.TAG_PIXELS))\n"
                     "                || (source.hasTag() && source.getTag().sizeInBytes() > MAX_SOURCE_NBT_BYTES))) return false;",
                     "        ItemStack source = getPhotograph();\n"
                     "        CustomData sourceData = source.get(DataComponents.CUSTOM_DATA);\n"
                     "        CompoundTag sourceTag = sourceData == null ? null : sourceData.copyTag();\n"
                     "        if (!source.isEmpty() && (!isSource(source)\n"
                     "                || (sourceTag != null && sourceTag.contains(PhotographData.TAG_PIXELS))\n"
                     "                || (sourceTag != null && sourceTag.sizeInBytes() > MAX_SOURCE_NBT_BYTES))) return false;")
menu = replace_exact(menu,
                     "                if (source.hasTag()) output.setTag(source.getTag().copy());",
                     "                CustomData photoData = source.get(DataComponents.CUSTOM_DATA);\n"
                     "                if (photoData != null) output.set(DataComponents.CUSTOM_DATA, photoData);")
assert menu == migrated("frame/FrameWorkbenchMenu.java"), "private frame workbench gameplay changed outside API rewrites"

proof = (root / "tools/port-probes/GuaniaoFrameDesignComponentProof.java").read_text("utf-8")
for required in (
    "safeForWorkbench(ItemStack stack, int maxBytes)",
    "data.copyTag().sizeInBytes() <= maxBytes",
    "copyPhotoReference(ItemStack source, ItemStack photograph)",
    "photograph.set(DataComponents.CUSTOM_DATA, data)",
):
    assert required in proof, "missing 26.2 CustomData compile proof: " + required

print("PASS: original photo source size/pixel rejection and private frame-workbench craft flow unchanged")
print("PASS: original async legacy pixel conversion and ownership safeguards unchanged")
print("PASS: real Java 25 source-data and output-copy API compile proof registered")
