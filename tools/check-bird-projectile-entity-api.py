#!/usr/bin/env python3
"""Compare Minecraft 26.2 entity-data/side-sync moves with byte-for-byte authored gameplay."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
FILES = (
    "src/main/java/EdDYON/guaniao/content/dropping/BirdDroppingProjectileEntity.java",
    "src/main/java/EdDYON/guaniao/content/dropping/BirdDroppingSplatEntity.java",
    "src/main/java/EdDYON/guaniao/content/fan/FeatherFanProjectileEntity.java",
    "src/main/java/EdDYON/guaniao/content/camera/PhotographEntity.java",
)

def expected_port(source, photograph):
    source = source.replace(
        "protected void defineSynchedData()",
        "protected void defineSynchedData(SynchedEntityData.Builder builder)")
    source = source.replace("super.defineSynchedData();", "super.defineSynchedData(builder);")
    source = source.replace("this.entityData.define(", "builder.define(")
    source = source.replace("this.getEntityData().define(", "builder.define(")
    source = re.sub(r"\.isClientSide\b(?!\s*\()", ".isClientSide()", source)
    source = re.sub(r"\bhasImpulse\b", "needsSync", source)
    # Minecraft 26.2 moved both vanilla mob classes; this is an import-only
    # namespace migration and does not alter bird-dropping hit behavior.
    source = source.replace(
        "import net.minecraft.world.entity.animal.IronGolem;",
        "import net.minecraft.world.entity.animal.golem.IronGolem;")
    source = source.replace(
        "import net.minecraft.world.entity.npc.Villager;",
        "import net.minecraft.world.entity.npc.villager.Villager;")
    if photograph:
        source = source.replace("import net.minecraft.resources.ResourceLocation;",
                                "import net.minecraft.resources.Identifier;")
        source = source.replace("import net.minecraftforge.registries.ForgeRegistries;\n", "")
        source = source.replace("ResourceLocation", "Identifier")
        source = source.replace("ForgeRegistries.ITEMS.getKey(",
                                "BuiltInRegistries.ITEM.getKey(")
    return source

for path in FILES:
    authored = subprocess.check_output(
        ["git", "show", "origin/main:" + path], cwd=ROOT,
        text=True).replace("\r\n", "\n")
    current = (ROOT / path).read_text("utf-8").replace("\r\n", "\n")
    expected = expected_port(authored, path.endswith("/PhotographEntity.java"))
    assert current == expected, f"{Path(path).name}: original projectile/photo gameplay changed"
    assert "protected void defineSynchedData(SynchedEntityData.Builder builder)" in current
    assert ".isClientSide &&" not in current
    assert "hasImpulse" not in current
    print("PASS:", Path(path).name, "original behavior retained")
assert len(FILES) == 4
