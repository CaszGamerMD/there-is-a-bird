#!/usr/bin/env python3
"""Report incompatible loader APIs in the Bird Watching Fabric 26.2 migration.

The report does not equate clean imports with parity: verify actual behaviors too.
Run: python3 tools/port-audit.py [--strict] [--json report.json]
"""
import argparse
import collections
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src" / "main" / "java"
RES = ROOT / "src" / "main" / "resources"
PATTERNS = {
    "forge": re.compile(r"\b(?:net\.minecraftforge|net\.neoforged|cpw\.mods|net\.minecraftforge\.fml)\."),
    "old_geckolib": re.compile(r"\bsoftware\.bernie\.geckolib\."),
    "mixin": re.compile(r"\b(?:org\.spongepowered\.asm\.mixin|@Mixin)"),
    "camera_render": re.compile(r"\b(?:com\.mojang\.blaze3d|RenderSystem|PoseStack|Tesselator|BufferBuilder)\b"),
    "custom_network": re.compile(r"\b(?:SimpleChannel|SimpleImpl|FriendlyByteBuf|PacketDistributor|CustomPacketPayload|NetworkRegistry)\b"),
}
EXTENSIONS = {".png", ".ogg", ".json", ".geo", ".animation", ".fsh", ".vsh"}
def main():
    p = argparse.ArgumentParser()
    p.add_argument("--strict", action="store_true", help="Fail if incompatible Forge/NeoForge imports remain")
    p.add_argument("--json", dest="json_path", help="Save machine-readable report")
    args = p.parse_args()
    counts = collections.Counter()
    sample = collections.defaultdict(list)
    java_files = sorted(SRC.rglob("*.java"))
    for file in java_files:
        text = file.read_text(encoding="utf-8-sig")
        for label, pattern in PATTERNS.items():
            if pattern.search(text):
                counts[label] += 1
                if len(sample[label]) < 25:
                    sample[label].append(file.relative_to(ROOT).as_posix())
    assets = collections.Counter(f.suffix for f in RES.rglob("*") if f.is_file() and f.suffix in EXTENSIONS)
    bird_folders = sorted({
        file.relative_to(SRC / "EdDYON" / "guaniao" / "content" / "bird").parts[0]
        for file in (SRC / "EdDYON" / "guaniao" / "content" / "bird").rglob("*Entity.java")
        if file.is_file()
    })
    result = {
        "target": "Minecraft 26.2 / Fabric",
        "java_file_count": len(java_files),
        "legacy_api_files": dict(counts),
        "legacy_api_samples": dict(sample),
        "bird_species_code_folders": bird_folders,
        "asset_file_counts_by_extension": dict(assets),
        "port_ready": not counts["forge"] and not counts["old_geckolib"],
        "note": "This is a source audit only. A compilation, server/client load and in-game parity tests are mandatory.",
    }
    print(json.dumps(result, indent=2, ensure_ascii=False))
    if args.json_path:
        Path(args.json_path).write_text(json.dumps(result, indent=2), encoding="utf-8")
    if args.strict and not result["port_ready"]:
        raise SystemExit("Port incomplete: Forge or old GeckoLib references still exist.")
if __name__ == "__main__":
    main()
