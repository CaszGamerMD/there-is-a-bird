#!/usr/bin/env python3
"""Gradual 26.2 bird enum relocation; preserve every original spawn/path condition."""
from pathlib import Path
import subprocess
import re

ROOT = Path(__file__).resolve().parents[1]
NAMES = ["BudgerigarEntity.java","CassowaryEntity.java","CockatielEntity.java","CockatooEntity.java","AbstractColumbidEntity.java","PigeonEntity.java","SpottedDoveEntity.java","CrowEntity.java","HummingbirdEntity.java","KestrelEntity.java","KingfisherEntity.java","KiwiEntity.java","LongTailedTitEntity.java","MacawEntity.java","MynaEntity.java","NightHeronEntity.java","SeagullEntity.java","SparrowEntity.java","UmbrellaCockatooEntity.java","WoodcockEntity.java"]
IDENTIFIER_PORTED = ("CrowEntity.java", "KestrelEntity.java",
                     "CockatooEntity.java", "CockatielEntity.java")
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
    synced_ported = ["BudgerigarEntity.java","CassowaryEntity.java","CockatielEntity.java","AbstractColumbidEntity.java","PigeonEntity.java","SpottedDoveEntity.java","CrowEntity.java","HummingbirdEntity.java","KestrelEntity.java","KingfisherEntity.java","KiwiEntity.java","MynaEntity.java","NightHeronEntity.java","SeagullEntity.java","SparrowEntity.java","UmbrellaCockatooEntity.java","WoodcockEntity.java"]
    if "software.bernie.geckolib" not in original:
        expected = original
        for old, new in RENAMES:
            expected = expected.replace(old, new)
        if path.name in synced_ported:
            expected = expected.replace("protected void defineSynchedData()",
                "protected void defineSynchedData(SynchedEntityData.Builder builder)")
            expected = expected.replace("super.defineSynchedData();", "super.defineSynchedData(builder);")
            expected = expected.replace("this.entityData.define(", "builder.define(")
            expected = expected.replace("entityData.define(", "builder.define(")
        expected = expected.replace(
            "import javax.annotation.Nullable;",
            "import org.jetbrains.annotations.Nullable;")
        client_side_ported = ["BudgerigarEntity.java","CassowaryEntity.java","CockatielEntity.java","AbstractColumbidEntity.java","SpottedDoveEntity.java","CrowEntity.java","HummingbirdEntity.java","KestrelEntity.java","KingfisherEntity.java","KiwiEntity.java","LongTailedTitEntity.java","MacawEntity.java","MynaEntity.java","NightHeronEntity.java","SeagullEntity.java","SparrowEntity.java","UmbrellaCockatooEntity.java","WoodcockEntity.java"]
        if path.name in client_side_ported:
            expected = re.sub(r"\.isClientSide\b(?!\s*\()", ".isClientSide()", expected)
        if path.name in ["BudgerigarEntity.java","AbstractColumbidEntity.java","CrowEntity.java","KestrelEntity.java","NightHeronEntity.java"]:
            expected = expected.replace("getMinBuildHeight()", "getMinY()")
            expected = expected.replace("getMaxBuildHeight()", "getMaxY()")
        if path.name == "PigeonEntity.java":
            expected = expected.replace(
                'compoundTag == null || !compoundTag.contains("PigeonVariant", 3)',
                'compoundTag == null || compoundTag.getInt("PigeonVariant").isEmpty()')
            expected = expected.replace(
                'ColumbidVariant.pigeonByOrdinal(compoundTag.getInt("PigeonVariant"))',
                'ColumbidVariant.pigeonByOrdinal(compoundTag.getInt("PigeonVariant").orElse(ColumbidVariant.GRAY_PIGEON.ordinal()))')
            expected = expected.replace(
                'GuaniaoEntityTypes.PIGEON.get().create(level)',
                'GuaniaoEntityTypes.PIGEON.get().create(level, EntitySpawnReason.BREEDING)')
        if path.name in ["BudgerigarEntity.java","AbstractColumbidEntity.java","CrowEntity.java","NightHeronEntity.java","SeagullEntity.java","SparrowEntity.java","WoodcockEntity.java"]:
            expected = re.sub(r"\bhasImpulse\b", "needsSync", expected)
        assert current == expected, f"{path.name}: unrelated gameplay edit"
    seen += 1
assert seen == len(NAMES), f"Expected {len(NAMES)} ported files, saw {seen}"
print(f"PASS: {seen} entity type migrations keep original behavior")
