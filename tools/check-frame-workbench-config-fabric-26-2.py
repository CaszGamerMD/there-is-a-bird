#!/usr/bin/env python3
"""Guard frame crafting bounds and pixel rounding when replacing ForgeConfigSpec."""
import math
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
rel = "src/main/java/EdDYON/guaniao/config/FrameWorkbenchConfig.java"
old = subprocess.check_output(["git", "show", "origin/main:" + rel], cwd=root, text=True)
new = (root / rel).read_text("utf-8")

def between(source, start, end):
    start_at = source.index(start)
    return source[start_at:source.index(end, start_at)]

for start, end in (
    ("        double lower = Math.min(minBlocks(), maxBlocks());", "    public static int maxSizePixels()"),
    ("        double upper = Math.max(minBlocks(), maxBlocks());", "    private static double minBlocks()"),
    ("    public static boolean allows(FrameDesign design) {", "\n}"),
):
    assert between(old, start, end) == between(new, start, end), "Frame size rounding or crafted-design bounds changed"

assert "net.minecraftforge" not in new and "ForgeConfigSpec" not in new
for key in ('"frameWorkbench"', '"minSizeBlocks"', '"maxSizeBlocks"', '"frame-workbench.json"'):
    if key.startswith('"minSize') or key.startswith('"maxSize'):
        # These Gson field names must survive serialization exactly.
        assert key.strip('"') in new
    else:
        assert key in new
assert "Double.isFinite(value)" in new and "Math.min(32.0D, value)" in new
assert "copy.minSizeBlocks = bounded(input.minSizeBlocks, 0.0D)" in new
assert "copy.maxSizeBlocks = bounded(input.maxSizeBlocks, 10.0D)" in new
assert "FrameWorkbenchConfig.loadOrCreateDefaults();" in (
    root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text("utf-8")

def limits(low, high):
    return min(512, math.ceil(min(low, high) * 16)), max(
        min(512, math.ceil(min(low, high) * 16)), min(512, math.floor(max(low, high) * 16)))

for low, high, expected in (
    (0, 10, (0, 160)), (12, 4, (64, 192)), (0.03, 0.04, (1, 1)),
    (32, 32, (512, 512)), (5, 10, (80, 160)),
):
    assert limits(low, high) == expected
print("FRAME CONFIG OK: Fabric JSON defaults, 0–32-block bounds and original pixel-grid rules.")
