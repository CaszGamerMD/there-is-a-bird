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
    if p.name in ("CrowEntity.java", "KestrelEntity.java", "MacawEntity.java", "BudgerigarEntity.java",
                   "AbstractColumbidEntity.java", "CockatooEntity.java", "CockatielEntity.java"):
        expected = expected.replace("ResourceLocation", "Identifier")
        if p.name == "BudgerigarEntity.java":
            expected = expected.replace(
                'new Identifier("guaniao", "husbandry/chirpy_partner")',
                'Identifier.fromNamespaceAndPath("guaniao", "husbandry/chirpy_partner")')
            expected = expected.replace(
                'serverPlayer.server.getAdvancements().getAdvancement(CHIRPY_PARTNER_ADVANCEMENT)',
                'serverPlayer.getServer().getAdvancements().get(CHIRPY_PARTNER_ADVANCEMENT)')
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
    client_side_ported = ["BudgerigarEntity.java","CassowaryEntity.java","CockatielEntity.java","AbstractColumbidEntity.java","SpottedDoveEntity.java","CrowEntity.java","HummingbirdEntity.java","KestrelEntity.java","KingfisherEntity.java","KiwiEntity.java","LongTailedTitEntity.java","MacawEntity.java","MynaEntity.java","NightHeronEntity.java","SeagullEntity.java","SparrowEntity.java","UmbrellaCockatooEntity.java","WoodcockEntity.java"]
    if p.name in client_side_ported:
        expected = re.sub(r"\.isClientSide\b(?!\s*\()", ".isClientSide()", expected)
    # World-height access moved to getMinY/getMaxY in Mojang 26.2.
    if p.name in ["BudgerigarEntity.java","AbstractColumbidEntity.java","CrowEntity.java","KestrelEntity.java","NightHeronEntity.java"]:
        expected = expected.replace("getMinBuildHeight()", "getMinY()")
        expected = expected.replace("getMaxBuildHeight()", "getMaxY()")
    # Minecraft replaced Entity.hasImpulse with the equivalent needsSync
    # flag; never change the original locations or conditions of writes.
    if p.name in ["BudgerigarEntity.java","AbstractColumbidEntity.java","CrowEntity.java","NightHeronEntity.java","SeagullEntity.java","SparrowEntity.java","WoodcockEntity.java"]:
        expected = re.sub(r"\bhasImpulse\b", "needsSync", expected)
    if relative.endswith("/hummingbird/HummingbirdEntity.java"):
        expected = expected.replace(
            "import software.bernie.geckolib.core.animation.*;",
            "import com.geckolib.animation.AnimationController;\nimport com.geckolib.animation.RawAnimation;")
        expected = expected.replace("new AnimationController<>(this, ", "new AnimationController<>(")
        # Typed ValueInput/ValueOutput serialization and pre-tame veto are
        # separately guarded by check-hummingbird-persistence-26-2.py. Keep
        # comparing the rest of the authored bird AI/animation byte-for-byte.
        expected = expected.replace("import net.minecraft.nbt.CompoundTag;\n", "")
        expected = expected.replace("import net.minecraft.resources.ResourceLocation;", "import net.minecraft.resources.Identifier;")
        expected = expected.replace("import net.minecraftforge.event.ForgeEventFactory;\n", "")
        expected = expected.replace("import net.minecraft.core.BlockPos;\n", "import net.minecraft.core.BlockPos;\nimport net.minecraft.core.UUIDUtil;\n")
        expected = expected.replace("import net.minecraft.world.level.ServerLevelAccessor;\n",
                                    "import net.minecraft.world.level.ServerLevelAccessor;\nimport net.minecraft.world.level.storage.ValueInput;\nimport net.minecraft.world.level.storage.ValueOutput;\n")
        expected = expected.replace("import EdDYON.guaniao.event.BirdPopulationTracker;\n",
                                    "import EdDYON.guaniao.event.BirdPopulationTracker;\nimport EdDYON.guaniao.event.BirdTamePermissionEvents;\n")
        expected = expected.replace("import com.geckolib.animatable.instance.AnimatableInstanceCache;\n",
                                    "import com.geckolib.animatable.instance.AnimatableInstanceCache;\nimport com.geckolib.animatable.manager.AnimatableManager;\n")
        expected = expected.replace('new ResourceLocation("guaniao", ', 'Identifier.fromNamespaceAndPath("guaniao", ')
        expected = expected.replace("!ForgeEventFactory.onAnimalTame(this, player)",
                                    "BirdTamePermissionEvents.BEFORE_TAME.invoker().canTame(this, player)")
    if relative.endswith(("/crow/CrowEntity.java", "/kestrel/KestrelEntity.java", "/seagull/SeagullEntity.java")):
        expected = expected.replace("!net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)",
                                    "EdDYON.guaniao.event.BirdTamePermissionEvents.BEFORE_TAME.invoker().canTame(this, player)")

    # The original cancellable Forge fish-capture event is now an equivalent
    # Fabric callback. Keep the cancellation BEFORE discarding the fish.
    if relative.endswith("/nightheron/NightHeronEntity.java"):
        expected = expected.replace("import net.minecraftforge.event.ForgeEventFactory;\n", "")
        expected = expected.replace("import net.minecraftforge.common.MinecraftForge;\n", "")
        expected = expected.replace(
            "MinecraftForge.EVENT_BUS.post(new NightHeronFishCaptureEvent(this, fish, purpose))",
            "NightHeronFishCaptureEvent.isCancelled(this, fish, purpose)")

    current = p.read_text("utf-8")
    if relative.endswith("/hummingbird/HummingbirdEntity.java"):
        start = "    @Override public void addAdditionalSaveData(CompoundTag tag) {"
        replacement_start = "    @Override protected void addAdditionalSaveData(ValueOutput output) {"
        end = "    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {"
        assert start in expected and replacement_start in current and end in expected and end in current
        expected = expected[:expected.index(start)] + current[current.index(replacement_start):current.index(end)] + expected[expected.index(end):]
    # Git stores normalized line endings; CRLF in individual source files is
    # immaterial to this preservation test.
    expected = expected.replace("\r\n", "\n")
    current = current.replace("\r\n", "\n")
    if current != expected:
        if relative.endswith("/hummingbird/HummingbirdEntity.java"):
            import difflib
            print("".join(list(difflib.unified_diff(expected.splitlines(True), current.splitlines(True), fromfile="expected", tofile="current"))[:90]))
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
