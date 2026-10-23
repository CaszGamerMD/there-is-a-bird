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
    if migrated != expected:
        raise SystemExit(f"{rel}: the block entity logic was modified beyond its GeckoLib 5 imports")
    print(f"GECKOLIB PARITY OK: {rel} unchanged except for four GeckoLib 5 imports")
