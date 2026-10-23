#!/usr/bin/env python3
"""Check GeckoLib 5 bird entity edits against original Forge source.

The migration is still incomplete. This check preserves every original AI
and animation decision outside the explicitly listed GeckoLib API mappings.
"""
from pathlib import Path
import subprocess

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
