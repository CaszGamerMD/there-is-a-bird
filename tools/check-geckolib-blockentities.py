#!/usr/bin/env python3
"""Assert migrated GeckoLib block entity implementations differ only in API imports.

This is source-preservation validation, not proof of runtime animation parity.
"""
import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[1]
relative_files = (
    "content/cage/BirdCageBlockEntity.java",
    "content/bath/BirdBathBlockEntity.java",
    "content/food/BaggedFriesBlockEntity.java",
    "content/nest/CrowNestBlockEntity.java",
)
old_to_new = {
    "software.bernie.geckolib.animatable.GeoBlockEntity": "com.geckolib.animatable.GeoBlockEntity",
    "software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache": "com.geckolib.animatable.instance.AnimatableInstanceCache",
    "software.bernie.geckolib.core.animation.AnimatableManager": "com.geckolib.animatable.manager.AnimatableManager",
    "software.bernie.geckolib.util.GeckoLibUtil": "com.geckolib.util.GeckoLibUtil",
}
for rel in relative_files:
    path = "src/main/java/EdDYON/guaniao/" + rel
    original = subprocess.check_output(["git", "show", "origin/main:" + path], cwd=root, text=True)
    migrated = (root / path).read_text("utf-8")
    expected = original
    for previous, updated in old_to_new.items():
        if f"import {previous};" not in original:
            raise SystemExit(f"Original GeckoLib import unexpectedly absent: {rel}: {previous}")
        expected = expected.replace(previous, updated)
    if rel == "content/food/BaggedFriesBlockEntity.java":
        # Minecraft 26.2 replaced the old Forge CompoundTag persistence and
        # client packet receive hooks. Verify that ALL code outside these
        # hooks remains identical to the original, including gameplay.
        expected = expected.replace(
            "import net.minecraft.nbt.CompoundTag;",
            "\n".join((
                "import net.minecraft.core.HolderLookup;",
                "import net.minecraft.nbt.CompoundTag;",
                "import net.minecraft.world.level.storage.ValueInput;",
                "import net.minecraft.world.level.storage.ValueOutput;",
            )))
        expected = expected.replace("import net.minecraft.network.Connection;\n", "")
        start = "    @Override\n    protected void saveAdditional(CompoundTag tag) {"
        migrated_start = "    @Override\n    protected void saveAdditional(ValueOutput output) {"
        end = "    @Override\n    public void registerControllers("
        prior_region = expected[expected.index(start):expected.index(end)]
        updated_region = migrated[migrated.index(migrated_start):migrated.index(end)]
        expected = expected.replace(prior_region, updated_region, 1)
        for old_token, new_token in (
            ("tag.putInt(FRY_MASK_TAG, this.fryMask)", "output.putInt(FRY_MASK_TAG, this.fryMask)"),
            ("tag.putBoolean(LAXATIVE_TAG, this.laxative)", "output.putBoolean(LAXATIVE_TAG, this.laxative)"),
            ("this.fryMask = tag.getInt(FRY_MASK_TAG) & FULL_MASK", "this.fryMask = input.getIntOr(FRY_MASK_TAG, 0) & FULL_MASK"),
            ("this.laxative = tag.getBoolean(LAXATIVE_TAG)", "this.laxative = input.getBooleanOr(LAXATIVE_TAG, false)"),
        ):
            if old_token not in prior_region or new_token not in updated_region:
                raise SystemExit(f"Bagged fries persistent game state lost in 26.2 migration: {old_token}")
        if ("count <= 0 ? 0 : (1 << Math.min(count, BaggedFriesBlock.TOTAL_FRIES)) - 1"
                not in updated_region):
            raise SystemExit("Bagged fries preexisting world block-state fallback changed")
        if "input.getInt(FRY_MASK_TAG).isPresent()" not in updated_region:
            raise SystemExit("Bagged fries explicit zero mask must survive reload without fallback")
        if "getUpdateTag(HolderLookup.Provider registries)" not in updated_region:
            raise SystemExit("Bagged fries client block update synchronization missing")
    if migrated != expected:
        raise SystemExit(f"{rel}: block entity gameplay differs outside explicitly migrated GeckoLib/persistence APIs")
    print(f"GECKOLIB PARITY OK: {rel} original behavior retained, allowing 26.2 persistence in fries")
