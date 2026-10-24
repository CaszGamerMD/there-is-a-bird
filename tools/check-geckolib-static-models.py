#!/usr/bin/env python3
"""Assert unchanged authored assets in five constant GeoModel GeckoLib 5 ports."""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
paths = (
    "client/food/BaggedFriesModel.java",
    "client/camera/NikonD750ItemModel.java",
    "client/nest/CrowNestModel.java",
    "client/nest/CrowNestItemModel.java",
    "client/dropping/BirdDroppingProjectileModel.java",
)
for item in paths:
    path = "src/main/java/EdDYON/guaniao/" + item
    original = subprocess.check_output(["git", "show", "origin/main:" + path], cwd=root, text=True)
    ported = (root / path).read_text(encoding="utf-8")
    expected = original.replace(
        "import software.bernie.geckolib.model.GeoModel;",
        "import com.geckolib.model.GeoModel;\nimport com.geckolib.renderer.base.GeoRenderState;",
    ).replace("ResourceLocation", "Identifier").replace(
        "new Identifier(", "Identifier.fromNamespaceAndPath(",
    )
    expected, changed = re.subn(
        r"public Identifier get(Model|Texture)Resource\(([^)]+)\)",
        lambda match: f"public Identifier get{match.group(1)}Resource(GeoRenderState renderState)",
        expected,
    )
    if changed != 2 or expected != ported:
        raise SystemExit("Unexpected asset/method drift in GeckoLib5 static model: " + path)
    print("GECKOLIB 5 STATIC MODEL PARITY OK:", item)
