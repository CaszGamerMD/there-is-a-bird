#!/usr/bin/env python3
"""Check GeckoLib 5 bird entity edits against original Forge source.

The migration is still incomplete. This check preserves every original AI
and animation decision outside the explicitly listed GeckoLib API mappings.
"""
from pathlib import Path
import subprocess
import re

root = Path(__file__).resolve().parents[1]
subtree = Path("src/main/java/EdDYON/guaniao/content/bird")
renames = {
    "software.bernie.geckolib.animatable.GeoEntity": "com.geckolib.animatable.GeoEntity",
    "software.bernie.geckolib.core.animatable.GeoAnimatable": "com.geckolib.animatable.GeoAnimatable",
    "software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache": "com.geckolib.animatable.instance.AnimatableInstanceCache",
    "software.bernie.geckolib.core.animation.AnimatableManager": "com.geckolib.animatable.manager.AnimatableManager",
    "software.bernie.geckolib.core.animation.AnimationController": "com.geckolib.animation.AnimationController",
    "software.bernie.geckolib.core.animation.AnimationState": "com.geckolib.animation.state.AnimationTest",
    "software.bernie.geckolib.core.animation.RawAnimation": "com.geckolib.animation.RawAnimation",
    "software.bernie.geckolib.core.object.PlayState": "com.geckolib.animation.object.PlayState",
    "software.bernie.geckolib.util.GeckoLibUtil": "com.geckolib.util.GeckoLibUtil",
}

count = 0
for p in sorted((root / subtree).rglob("*Entity.java")):
    relative = p.relative_to(root).as_posix()
    original = subprocess.check_output(["git", "show", "origin/main:" + relative], cwd=root, text=True)
    if "software.bernie.geckolib" not in original:
        continue

    expected = original
    for old, new in renames.items():
        expected = expected.replace(old, new)
    expected = expected.replace("AnimationState<", "AnimationTest<")
    expected = expected.replace(".getController()", ".controller()")
    expected = expected.replace(".transitionLength(", ".setTransitionTicks(")
    # Ported 26.2 bird enum package/type names; no changes to comparisons or values.
    enum_ported = ["BudgerigarEntity.java","CassowaryEntity.java","CockatielEntity.java","CockatooEntity.java","AbstractColumbidEntity.java","PigeonEntity.java","SpottedDoveEntity.java","CrowEntity.java","HummingbirdEntity.java","KestrelEntity.java","KingfisherEntity.java","KiwiEntity.java","LongTailedTitEntity.java","MacawEntity.java","MynaEntity.java","NightHeronEntity.java","SeagullEntity.java","SparrowEntity.java","UmbrellaCockatooEntity.java","WoodcockEntity.java"]
    if p.name in enum_ported:
        expected = expected.replace("MobSpawnType", "EntitySpawnReason")
        expected = expected.replace("BlockPathTypes", "PathType")
        expected = expected.replace("net.minecraft.world.entity.animal.AbstractFish",
                                    "net.minecraft.world.entity.animal.fish.AbstractFish")
        expected = expected.replace("net.minecraft.world.level.chunk.ChunkStatus",
                                    "net.minecraft.world.level.chunk.status.ChunkStatus")
    # Minecraft 26.2 ResourceLocation -> Identifier retains all authored IDs,
    # including Kestrel's serialized learned-item name and Crow's texture path.
    if p.name in ("CrowEntity.java", "KestrelEntity.java"):
        expected = expected.replace("ResourceLocation", "Identifier")
    # 26.2 removed FlyingAnimal; preserve its omnidirectional flight semantics
    # by overriding the new Entity method while leaving isFlying() body intact.
    if "import net.minecraft.world.entity.animal.FlyingAnimal;" in expected:
        expected = expected.replace("import net.minecraft.world.entity.animal.FlyingAnimal;\n", "")
        if "implements FlyingAnimal," in expected:
            expected = expected.replace("implements FlyingAnimal,", "implements", 1)
        else:
            expected = expected.replace(", FlyingAnimal", "", 1)
        flying_override = __import__("re").compile(
            r"(^[ \t]*)@Override[ \t]*(?:\n[ \t]*)?public boolean isFlying\(\)",
            __import__("re").M)
        def _flight_hook(match):
            pad = match.group(1)
            return (
                pad + "@Override\n"
                + pad + "protected boolean omnidirectionalAirMover() {\n"
                + pad + "    return true;\n"
                + pad + "}\n\n"
                + pad + "public boolean isFlying()"
            )
        expected, hook_count = flying_override.subn(_flight_hook, expected, count=1)
        if hook_count != 1:
            raise SystemExit(f"{relative}: FlyingAnimal conversion lost isFlying")
    # 26.2 SynchedEntityData.Builder keeps each original accessor/default value,
    # and calls the superclass initializer before defining subclass fields.
    synced_ported = ["BudgerigarEntity.java","CassowaryEntity.java","CockatielEntity.java","AbstractColumbidEntity.java","PigeonEntity.java","SpottedDoveEntity.java","CrowEntity.java","HummingbirdEntity.java","KestrelEntity.java","KingfisherEntity.java","KiwiEntity.java","MynaEntity.java","NightHeronEntity.java","SeagullEntity.java","SparrowEntity.java","UmbrellaCockatooEntity.java","WoodcockEntity.java"]
    if p.name in synced_ported:
        expected = expected.replace(
            "protected void defineSynchedData()",
            "protected void defineSynchedData(SynchedEntityData.Builder builder)")
        expected = expected.replace("super.defineSynchedData();", "super.defineSynchedData(builder);")
        expected = expected.replace("this.entityData.define(", "builder.define(")
        expected = expected.replace("entityData.define(", "builder.define(")
    # 26.2 dependency supplies JetBrains nullability annotations rather
    # than the old javax.annotation.Nullable package.
    expected = expected.replace(
        "import javax.annotation.Nullable;",
        "import org.jetbrains.annotations.Nullable;")
    client_side_ported = ["BudgerigarEntity.java","CassowaryEntity.java","CockatielEntity.java","AbstractColumbidEntity.java","SpottedDoveEntity.java","CrowEntity.java","HummingbirdEntity.java","KestrelEntity.java","KingfisherEntity.java"]
    if p.name in client_side_ported:
        expected = re.sub(r"\.isClientSide\b(?!\s*\()", ".isClientSide()", expected)
    if relative.endswith("/hummingbird/HummingbirdEntity.java"):
        expected = expected.replace(
            "import software.bernie.geckolib.core.animation.*;",
            "import com.geckolib.animation.AnimationController;\nimport com.geckolib.animation.RawAnimation;")
        expected = expected.replace("new AnimationController<>(this, ", "new AnimationController<>(")

    current = p.read_text("utf-8")
    # Git stores normalized line endings; CRLF in individual source files is
    # immaterial to this preservation test.
    expected = expected.replace("\r\n", "\n")
    current = current.replace("\r\n", "\n")
    if current != expected:
        raise SystemExit(f"{relative}: unexpected bird AI/animation logic changes in GeckoLib migration")
    if "software.bernie.geckolib" in current:
        raise SystemExit(f"{relative}: old GeckoLib API remains in a migrated bird entity")
    print(f"GECKO BIRD PARITY OK: {relative}")
    count += 1

if count < 15:
    raise SystemExit(f"Expected at least 15 migrated GeckoLib bird entity sources, found {count}")
print(f"GECKO BIRD PARITY OK: {count} bird entity files contain only listed GeckoLib API changes.")

# The original bird gait constants were authored for these specific animation
# clips. Switching the registry accessor must not alter even one constant.
stride_path = "src/main/java/EdDYON/guaniao/content/bird/BirdWalkStride.java"
before = subprocess.check_output(["git", "show", "origin/main:" + stride_path], cwd=root, text=True)
expected = before.replace("import net.minecraftforge.registries.ForgeRegistries;", "import net.minecraft.core.registries.BuiltInRegistries;")
expected = expected.replace(
    "ForgeRegistries.ENTITY_TYPES.getKey(bird.getType()).getPath()",
    "BuiltInRegistries.ENTITY_TYPE.getKey(bird.getType()).getPath()")
after = (root / stride_path).read_text("utf-8")
if after.replace("\r\n", "\n") != expected.replace("\r\n", "\n"):
    raise SystemExit("BirdWalkStride: authored bird gait constants or cadence rules changed")
print("GECKO BIRD PARITY OK: every original gait value and cadence limit retained")
