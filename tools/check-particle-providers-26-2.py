#!/usr/bin/env python3
"""All eight Forge particle types: GeckoLib-independent Fabric 26.2 visual parity."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
BASE = "src/main/java/EdDYON/guaniao/"

def old(path):
    return subprocess.check_output(["git", "show", "origin/main:" + BASE + path],
                                   cwd=ROOT, text=True).replace("\r\n", "\n")

def new(path):
    return (ROOT / (BASE + path)).read_text("utf-8").replace("\r\n", "\n")

particles = (
    ("KillFeather", "KILL_FEATHER"),
    ("BurialWind", "BURIAL_WIND"),
    ("BurialCyclone", "BURIAL_CYCLONE"),
    ("RivenSplit", "RIVEN_SPLIT"),
    ("RivenStreak", "RIVEN_STREAK"),
    ("HuntingMark", "HUNTING_MARK"),
    ("HuntingStreak", "HUNTING_STREAK"),
)

client = new("fabric/GuaniaoFabricClientEntrypoint.java")
for species, registration in particles:
    path = "client/particle/" + species + "Particle.java"
    before, after = old(path), new(path)
    # Normalize only the actual removed Minecraft 26.2 renderer/provider
    # interfaces. All physics, alpha, scale, sway and spin code stays verbatim.
    expected = before.replace(
        "import net.minecraft.client.particle.ParticleRenderType;",
        "import net.minecraft.client.particle.SingleQuadParticle;"
    ).replace(
        "import net.minecraft.client.particle.TextureSheetParticle;",
        "import net.minecraft.client.renderer.texture.TextureAtlasSprite;\nimport net.minecraft.util.RandomSource;"
    ).replace(
        "public final class " + species + "Particle extends TextureSheetParticle {",
        "public final class " + species + "Particle extends SingleQuadParticle {"
    ).replace(
        "SpriteSet sprites) {\n        super(level, x, y, z, xSpeed, ySpeed, zSpeed);\n        this.pickSprite(sprites);",
        "TextureAtlasSprite sprite) {\n        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);"
    ).replace(
        "SpriteSet sprites) {\n        super(level, x, y, z);\n        this.pickSprite(sprites);",
        "TextureAtlasSprite sprite) {\n        super(level, x, y, z, sprite);"
    ).replace(
        "public ParticleRenderType getRenderType() {\n        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;",
        "public SingleQuadParticle.Layer getLayer() {\n        return SingleQuadParticle.Layer.TRANSLUCENT;"
    ).replace(
        "double xSpeed, double ySpeed, double zSpeed) {\n            return new",
        "double xSpeed, double ySpeed, double zSpeed, RandomSource random) {\n            return new"
    ).replace("this.sprites);", "this.sprites.get(random));")
    assert expected == after, species + ": authored particle behavior changed beyond the 26.2 API"
    assert "TextureSheetParticle" not in after
    assert "RandomSource random)" in after
    assert "SingleQuadParticle.Layer.TRANSLUCENT" in after
    call = ("ParticleProviderRegistry.getInstance().register(GuaniaoParticleTypes."
            + registration + ".get(), " + species + "Particle.Provider::new);")
    assert call in client, species

assert "ParticleProviderRegistry.getInstance().register(GuaniaoParticleTypes.PLACEABLE_FLECK.get(), PlaceableBreakFleckParticle.Provider::new);" in client
assert client.count("ParticleProviderRegistry.getInstance().register(GuaniaoParticleTypes.") == 8
assert "net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry" in client
# Custom debris is created from its registered particle TYPE, then configured
# as a SingleQuadParticle. The effect intentionally does not name its provider
# implementation (registered separately in the client entrypoint).
effect = new("client/particle/PlaceableBlockBreakEffects.java")
assert "particles.createParticle(GuaniaoParticleTypes.PLACEABLE_FLECK.get()," in effect
assert "particle instanceof net.minecraft.client.particle.SingleQuadParticle quad" in effect
assert "quad.setColor(color.red, color.green, color.blue);" in effect
assert "quad.scale(scale);" in effect
assert "PlaceableBreakFleckParticle" in client

print("FABRIC PARTICLE PROVIDERS 26.2 PARITY OK: all 8 registered, all seven"
      " combat/special feathers preserve exact authored motion and fade formulas")
