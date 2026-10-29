#!/usr/bin/env python3
"""Check byte-preserving staging of original GeckoLib 4 model/animation JSON.

GeckoLib 5 expects resources under assets/guaniao/geckolib/{models,animations}.
The authored JSON remains in its original 1.20.1 paths in the repository,
and Gradle stages unchanged copies during processResources.
"""
from pathlib import Path
import json
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/guaniao"
GRADLE = (ROOT / "build.gradle").read_text("utf-8")
BRIDGE = (ROOT / "src/main/java/EdDYON/guaniao/client/entity/mutation/GuaniaoGeckoAssets.java").read_text("utf-8")

models = sorted((ASSETS / "geo").rglob("*.geo.json"))
animations = sorted((ASSETS / "animations").rglob("*.animation.json"))
assert len(models) >= 30, f"Original models unexpectedly missing: {len(models)}"
assert len(animations) >= 20, f"Original animations unexpectedly missing: {len(animations)}"

for original in models + animations:
    relative = original.relative_to(ROOT).as_posix()
    old = subprocess.check_output(["git", "show", "origin/main:"+relative],
                                  cwd=ROOT)
    assert original.read_bytes() == old, "Authored media content changed: " + relative
    json.loads(original.read_text("utf-8"))

assert "include '**/*.geo.json'" in GRADLE
assert "include '**/*.animation.json'" in GRADLE
assert "geckolib/models" in GRADLE
assert "geckolib/animations" in GRADLE
assert "from originalModels" in GRADLE and "from originalAnimations" in GRADLE
assert 'normalized(authored, "geo/", ".geo.json")' in BRIDGE
assert 'normalized(authored, "animations/", ".animation.json")' in BRIDGE
assert "Identifier.fromNamespaceAndPath(authored.getNamespace()" in BRIDGE

for stem in ("Kiwi", "Sparrow", "Seagull", "Myna", "LongTailedTit", "NightHeron", "Crow", "Macaw", "Budgerigar", "Columbid", "Cockatoo"):
    folders = {
        "LongTailedTit": "longtailedtit",
        "NightHeron": "nightheron"
    }
    folder = folders.get(stem, stem.lower())
    model = (ROOT / ("src/main/java/EdDYON/guaniao/client/entity/"
                      + folder + "/" + stem + "Model.java")).read_text("utf-8")
    assert "GuaniaoGeckoAssets.model(" in model, stem
    assert "GuaniaoGeckoAssets.animation(" in model, stem
    assert "BirdMutationRenderData.capture(" in model, stem

for path in ("client/food/BaggedFriesModel.java",
             "client/camera/NikonD750ItemModel.java",
             "client/nest/CrowNestModel.java",
             "client/nest/CrowNestItemModel.java",
             "client/dropping/BirdDroppingProjectileModel.java"):
    model = (ROOT / "src/main/java/EdDYON/guaniao" / path).read_text("utf-8")
    assert "GuaniaoGeckoAssets.model(MODEL)" in model, path
    assert "GuaniaoGeckoAssets.animation(ANIMATION)" in model, path

print(f"GECKOLIB 5 RESOURCE PARITY OK: {len(models)} original models and "
      f"{len(animations)} original animations retained byte-for-byte; "
      "build-time staging, eleven bird and five static lookups verified")
