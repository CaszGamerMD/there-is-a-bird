#!/usr/bin/env python3
"""Check GeckoLib 5 block entities retain the original gameplay and persistent data.

Four animated block-entity sources are compared with origin/main.
Bird baths and bagged fries now use Minecraft 26.2's ValueInput/ValueOutput.
All other code must still match the original after the documented API edits.
"""
from pathlib import Path
import re
import subprocess

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
persisted = {
    "content/food/BaggedFriesBlockEntity.java": (
        "FRY_MASK_TAG", "LAXATIVE_TAG",
    ),
    "content/nest/CrowNestBlockEntity.java": (
        "TREASURES_TAG", "NATURAL_NEST_TAG", "CROW_CLAIMS_TAG", "CLAIM_CROW_TAG",
        "CLAIM_TIME_TAG", "RUMMAGED_MASK_TAG", "CURRENT_SEARCH_SLOT_TAG",
        "SEARCH_TICKS_TAG", "SEARCH_TOTAL_TICKS_TAG", "LOOT_LAYOUT_SEED_TAG",
        "LOOT_FOOTPRINTS_TAG", "LOOT_COLUMNS_TAG", "LOOT_ROWS_TAG",
    ),
    "content/bath/BirdBathBlockEntity.java": (
        "CONTENT_TYPE_TAG", "CONTENT_LEVEL_TAG", "CLEANLINESS_TAG",
        "SPOILED_CONTENT_TYPE_TAG", "SPOIL_TICKS_TAG", "FROZEN_SUGAR_WATER_TAG",
        "ENVIRONMENTAL_TICK_OFFSET_TAG", "OCCUPIED_TICKS_TAG",
        "RECENT_BIRD_USE_TICKS_TAG", "CURRENT_USER_TAG",
    ),
}

for rel in relative_files:
    path = "src/main/java/EdDYON/guaniao/" + rel
    original = subprocess.check_output(
        ["git", "show", "origin/main:" + path], cwd=root, text=True)
    migrated = (root / path).read_text("utf-8")
    expected = original
    for previous, updated in old_to_new.items():
        if f"import {previous};" not in original:
            raise SystemExit(f"Original GeckoLib import unexpectedly absent: {rel}: {previous}")
        expected = expected.replace(previous, updated)
        if rel.startswith("content/nest/"):
            old_load = "    @Override\n    public void onLoad() {\n        super.onLoad();\n        if (this.level != null && !this.level.isClientSide) {\n            LOADED_NESTS.computeIfAbsent(this.level, ignored -> new HashSet<>()).add(this);\n        }\n    }"
            new_load = "    @Override\n    public void setLevel(Level level) {\n        // BlockEntity.onLoad no longer exists in 26.2. Index the nest when\n        // Minecraft assigns the level; setRemoved still unregisters it.\n        super.setLevel(level);\n        if (level != null && !level.isClientSide()) {\n            LOADED_NESTS.computeIfAbsent(level, ignored -> new HashSet<>()).add(this);\n        }\n    }"
            if old_load not in expected or new_load not in migrated:
                raise SystemExit("Crow nest load hook changed beyond 26.2 compatibility substitution")
            expected = expected.replace(old_load, new_load, 1)
            expected = expected.replace(".isClientSide", ".isClientSide()").replace(".isClientSide()()", ".isClientSide()")
            expected = expected.replace(
                "ItemStack.isSameItemSameTags(stored, incoming)",
                "ItemStack.isSameItemSameComponents(stored, incoming)")
            expected = expected.replace(
                "    @Override\n    public AABB getRenderBoundingBox()",
                "    public AABB getRenderBoundingBox()")
    if rel in persisted:
        # Only the imports and method region for save, load and packet syncing
        # may change; particle, animation and entity behavior remains identical.
        expected = expected.replace(
            "import net.minecraft.nbt.CompoundTag;",
            "\n".join((
                "import net.minecraft.core.HolderLookup;",
                "import net.minecraft.nbt.CompoundTag;",
                "import net.minecraft.world.level.storage.ValueInput;",
                "import net.minecraft.world.level.storage.ValueOutput;",
            )))
        expected = expected.replace("import net.minecraft.network.Connection;\n", "")
        if rel.startswith("content/nest/"):
            expected = expected.replace("import net.minecraft.nbt.ListTag;\n", "")
        old_start = "    @Override\n    protected void saveAdditional(CompoundTag tag) {"
        new_start = "    @Override\n    protected void saveAdditional(ValueOutput output) {"
        end = ("    public AABB getRenderBoundingBox()" if rel.startswith("content/nest/")
               else "    @Override\n    public void registerControllers(")
        old_region = expected[expected.index(old_start):expected.index(end)]
        new_region = migrated[migrated.index(new_start):migrated.index(end)]
        expected = expected.replace(old_region, new_region, 1)

        for key in persisted[rel]:
            if key not in old_region or key not in new_region:
                raise SystemExit(f"{rel}: persistent data field lost: {key}")
        old_write_fields = set(re.findall(
            r"tag\.put(?:IntArray|Int|Boolean|Long|UUID)\((\w+)_TAG\b", old_region))
        new_write_fields = set(re.findall(
            r"output\.put(?:Int|Boolean|Long|IntArray)\((\w+)_TAG\b", new_region))
        if old_write_fields != new_write_fields:
            raise SystemExit(f"{rel}: write keys changed: {old_write_fields} -> {new_write_fields}")
        if "loadAdditional(ValueInput input)" not in new_region:
            raise SystemExit(f"{rel}: missing 26.2 block-entity loader")
        if "getUpdateTag(HolderLookup.Provider registries)" not in new_region:
            raise SystemExit(f"{rel}: client update on chunk load lost")
        if "ClientboundBlockEntityDataPacket.create(this)" not in new_region:
            raise SystemExit(f"{rel}: client live update packet lost")

        if rel.startswith("content/food/"):
            for before, after in (
                ("tag.putInt(FRY_MASK_TAG, this.fryMask)",
                 "output.putInt(FRY_MASK_TAG, this.fryMask)"),
                ("tag.putBoolean(LAXATIVE_TAG, this.laxative)",
                 "output.putBoolean(LAXATIVE_TAG, this.laxative)"),
                ("this.fryMask = tag.getInt(FRY_MASK_TAG) & FULL_MASK",
                 "this.fryMask = input.getIntOr(FRY_MASK_TAG, 0) & FULL_MASK"),
                ("this.laxative = tag.getBoolean(LAXATIVE_TAG)",
                 "this.laxative = input.getBooleanOr(LAXATIVE_TAG, false)"),
            ):
                if before not in old_region or after not in new_region:
                    raise SystemExit("Bagged fries gameplay/save-field regression: " + before)
            if "input.getInt(FRY_MASK_TAG).isPresent()" not in new_region:
                raise SystemExit("Fries saved empty mask must not fall back to a full bag")
        elif rel.startswith("content/nest/"):
            for invariant in (
                "ContainerHelper.saveAllItems(output.child(TREASURES_TAG), this.treasures)",
                "ContainerHelper.loadAllItems(input.childOrEmpty(TREASURES_TAG), this.treasures)",
                "output.childrenList(CROW_CLAIMS_TAG)",
                "input.childrenListOrEmpty(CROW_CLAIMS_TAG)",
                "claim.putIntArray(CLAIM_CROW_TAG, uuidToIntArray(entry.getKey()))",
                "claim.getIntArray(CLAIM_CROW_TAG)",
                "this.rummagedMask = input.getIntOr(RUMMAGED_MASK_TAG, 0)",
                "this.currentSearchSlot = input.getIntOr(CURRENT_SEARCH_SLOT_TAG, -1)",
                "this.sanitizeStoredLoot(this.treasures.get(slot))",
                "this.clearLootPosition(slot)",
                "this.resetCurrentSearch()",
            ):
                if invariant not in new_region:
                    raise SystemExit("Crow nest inventory/progress regression: " + invariant)
        else:
            if "output.putIntArray(CURRENT_USER_TAG" not in new_region:
                raise SystemExit("Bird bath occupancy UUID was lost or saved in incompatible format")
            if "input.getIntArray(CURRENT_USER_TAG)" not in new_region:
                raise SystemExit("Bird bath cannot read its original UUID int-array tag")
            for invariant in (
                "this.contentType = BirdBathContentType.EMPTY;",
                "this.contentLevel = 0;",
                "this.spoiledContentType = BirdBathContentType.EMPTY;",
                "this.frozenSugarWater = this.isFrozen() &&",
                "this.currentUser = null;",
                "Math.max(0, input.getIntOr(SPOIL_TICKS_TAG, 0))",
                "Math.max(0, input.getIntOr(OCCUPIED_TICKS_TAG, 0))",
            ):
                if invariant not in new_region:
                    raise SystemExit("Bird bath missing original validity/fallback behavior: " + invariant)

    if migrated != expected:
        raise SystemExit(f"{rel}: gameplay differs outside documented GeckoLib/persistence changes")
    print(f"BLOCK ENTITY PARITY OK: {rel}")
