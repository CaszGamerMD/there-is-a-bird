#!/usr/bin/env python3
"""Keep the user's original FrameDesign NBT schema and version-3 wire format."""
from pathlib import Path
import re
import subprocess
ROOT=Path(__file__).resolve().parents[1]
PATH="src/main/java/EdDYON/guaniao/content/camera/frame/FrameDesign.java"
old=subprocess.check_output(["git","show","origin/main:"+PATH],cwd=ROOT,text=True).replace("\r\n","\n")
new=(ROOT/PATH).read_text("utf-8").replace("\r\n","\n")

# The exact authored storage keys and low-level buffer write/read order stay.
for method_start,method_end in (
    ("    public CompoundTag toTag() {","    @Nullable\n    public static FrameDesign fromTag("),
    ("    public void writeTo(FriendlyByteBuf buffer) {","    public static FrameDesign readFrom("),
):
    x=old[old.index(method_start):old.index(method_end)]
    y=new[new.index(method_start):new.index(method_end)]
    assert x==y,"Authored field names or serialization order changed in "+method_start
for term in ("public static final String TAG = \"FrameDesign\";",
             "public static final int GRID = 32;",
             "public static final int MAX_DECORATIONS = 16;",
             'tag.putInt("Version", 3);',
             'buffer.writeByte(3);'):
    assert term in old and term in new,term
assert "int version = tag.getIntOr(\"Version\",0);" in new
assert 'isTag(tag, "Mask", Tag.TAG_INT_ARRAY)' in new
for name in ("Width","Height","Corners","Border","Depth","Zoom","OffsetX","OffsetY","Rotation",
             "X","Y","Size"):
    assert f'tag.getIntOr("{name}",0)' in new,name
for name in ("Ornament","Template","Wood","Trim","Mat","Fit",
             "FrameMaterial","TrimMaterial","LiningMaterial","Type","Item"):
    assert f'tag.getStringOr("{name}","")' in new,name
for token in ("tag.getBooleanOr(\"Locked\",false)",
              "tag.getByteOr(\"Locked\",(byte)-1) < 0",
              "points.stream().anyMatch(point -> point.getId() != Tag.TAG_COMPOUND)",
              "points.getCompoundOrEmpty(i)"):
    assert token in new,token
for term in ("import net.minecraft.resources.Identifier;",
             "import net.minecraft.core.component.DataComponents;",
             "import net.minecraft.world.item.component.CustomData;",
             "CustomData.update(DataComponents.CUSTOM_DATA, stack,",
             "custom.put(TAG, design.toTag())",
             "getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()",
             "custom.get(TAG) instanceof CompoundTag tag ? fromTag(tag) : null",
             "Identifier.tryParse(value)", "BuiltInRegistries.ITEM.getValue("):
    assert term in new,term
for old_api in ("ResourceLocation", "getTagElement(", "addTagElement(",
                "tag.getInt(\"", "tag.getString(\"",
                "tag.getBoolean(\"", "tag.getByte(\"",
                "getElementType()", "points.getCompound(i)",
                "BuiltInRegistries.ITEM.get("):
    assert old_api not in new,old_api
# Verify that *all* enum constants, 32-row mask algorithms and frame
# validation limits survived; only Minecraft API relocations were made.
for enum in ("Template", "Wood", "Trim", "Mat", "PhotoFit", "DecorationType"):
    start="    public enum "+enum+" {"
    assert start in old and start in new,enum
old_mask=old[old.index("    /** Rectangles are disjoint"):old.index("\n    @Nullable",old.index("    /** Rectangles are disjoint"))]
new_mask=new[new.index("    /** Rectangles are disjoint"):new.index("\n    @Nullable",new.index("    /** Rectangles are disjoint"))]
assert old_mask==new_mask,"mask rectangle decomposition changed"
print("FRAME DESIGN 26.2 NBT PARITY OK: version-3 NBT and wire order,"
      " all item properties, authored masks and optional-modded IDs")
