#!/usr/bin/env python3
"""Gradual 26.2 bird enum relocation; preserve every original spawn/path condition."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
NAMES = ["BudgerigarEntity.java","CassowaryEntity.java","CockatielEntity.java","CockatooEntity.java","AbstractColumbidEntity.java","PigeonEntity.java","SpottedDoveEntity.java","CrowEntity.java","HummingbirdEntity.java","KestrelEntity.java","KingfisherEntity.java","KiwiEntity.java","LongTailedTitEntity.java","MacawEntity.java","MynaEntity.java","NightHeronEntity.java","SeagullEntity.java","SparrowEntity.java","UmbrellaCockatooEntity.java","WoodcockEntity.java"]
IDENTIFIER_PORTED = ("CrowEntity.java", "KestrelEntity.java")
RENAMES = (
    ("MobSpawnType", "EntitySpawnReason"),
    ("BlockPathTypes", "PathType"),
    ("net.minecraft.world.entity.animal.AbstractFish",
     "net.minecraft.world.entity.animal.fish.AbstractFish"),
    ("net.minecraft.world.level.chunk.ChunkStatus",
     "net.minecraft.world.level.chunk.status.ChunkStatus"),
)
base = ROOT / "src/main/java/EdDYON/guaniao/content/bird"
seen = 0
for path in sorted(base.rglob("*Entity.java")):
    if path.name not in NAMES:
        continue
    original = subprocess.check_output(
        ["git", "show", "origin/main:" + path.relative_to(ROOT).as_posix()],
        cwd=ROOT, text=True).replace("\r\n", "\n")
    current = path.read_text("utf-8").replace("\r\n", "\n")
    if path.name in IDENTIFIER_PORTED:
        assert "ResourceLocation" not in current
        assert "import net.minecraft.resources.Identifier;" in current
        assert "Identifier" in current
    for old, new in RENAMES:
        if old in original:
            assert new in current, f"{path.name}: missing renamed type {new}"
            assert old not in current, f"{path.name}: retired type {old} remains"
    # The existing GeckoLib parity test covers every migrated Gecko bird.
    # For subclass-only entities with no GeckoLib, protect their entire source.
    if "software.bernie.geckolib" not in original:
        expected = original
        for old, new in RENAMES:
            expected = expected.replace(old, new)
        assert current == expected, f"{path.name}: unrelated gameplay edit"
    seen += 1
assert seen == len(NAMES), f"Expected {len(NAMES)} ported files, saw {seen}"
print(f"PASS: {seen} entity type migrations keep original behavior")
