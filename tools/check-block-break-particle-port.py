#!/usr/bin/env python3
"""Ensure all six original Forge hit/destruction effects survive the 26.2 move."""
from pathlib import Path
import json
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
BASE = "src/main/java/EdDYON/guaniao/"

def original(path):
    return subprocess.check_output(
        ["git", "show", "origin/main:" + BASE + path], cwd=ROOT, text=True
    ).replace("\r\n", "\n")

def current(path):
    return (ROOT / (BASE + path)).read_text("utf-8").replace("\r\n", "\n")

old_fx = original("client/particle/PlaceableBlockBreakEffects.java")
new_fx = current("client/particle/PlaceableBlockBreakEffects.java")
visual_anchor = "    private static void spawnHitEffect("
# All the original numeric palette constants, spawning formulas, particle
# amounts and velocity distributions must remain exactly byte-identical.
assert old_fx[:old_fx.index("    public static IClientBlockExtensions baggedFries()")].replace(
    "import net.minecraftforge.client.extensions.common.IClientBlockExtensions;\n", ""
).replace("import net.minecraft.world.phys.HitResult;\n", "") == new_fx[:new_fx.index("    /**")]
# The only changed visual call is the mandatory 26.2 Particle ->
# SingleQuadParticle type guard: base Particle no longer owns setColor().
old_visual = old_fx[old_fx.index(visual_anchor):]
new_visual = new_fx[new_fx.index(visual_anchor):]
old_color = """        if (particle != null) {
            particle.setColor(color.red, color.green, color.blue);
            particle.scale(scale);
        }"""
new_color = """        if (particle instanceof net.minecraft.client.particle.SingleQuadParticle quad) {
            quad.setColor(color.red, color.green, color.blue);
            quad.scale(scale);
        }"""
assert old_color in old_visual and new_color in new_visual
assert old_visual.replace(old_color, new_color) == new_visual, (
    "An authored particle color/count/motion/shape formula changed"
)
assert "IClientBlockExtensions" not in new_fx and "net.minecraftforge" not in new_fx
for key in ("renderHit(", "renderDestroy(", "kindFor(BlockState state)",
            "spawnHitEffect(kind, state, level, position, particles);",
            "spawnDestroyEffect(kind, state, level, pos, particles);",
            "target.getBlockPos().equals(pos)", "return null;"):
    assert key in new_fx, key

blocks = {
    "content/bath/BirdBathBlock.java": ("birdBath", "BirdBathBlock"),
    "content/cage/BirdCageBlock.java": ("birdCage", "BirdCageBlock"),
    "content/nest/CrowNestBlock.java": ("crowNest", "CrowNestBlock"),
    "content/feed/BreadcrumbPileBlock.java": ("breadcrumbs", "BreadcrumbPileBlock"),
    "content/food/BaggedFriesBlock.java": ("baggedFries", "BaggedFriesBlock"),
    "content/dropping/BirdDroppingStainBlock.java": ("droppingStain", "BirdDroppingStainBlock"),
}
for path, (function, species) in blocks.items():
    original_block, changed = original(path), current(path)
    assert f"PlaceableBlockBreakEffects.{function}()" in original_block, path
    assert "initializeClient(Consumer<IClientBlockExtensions>" in original_block, path
    assert "initializeClient(" not in changed, path
    assert "net.minecraftforge.client.extensions.common.IClientBlockExtensions" not in changed, path
    assert "PlaceableBlockBreakEffects" not in changed, path
    # Some authored block classes are referenced by fully-qualified names
    # instead of imports. Check the actual instanceof type, not its spelling.
    assert re.search(
        r"\\binstanceof\\s+(?:EdDYON\\.guaniao\\.[\\w.]+\\.)?"
        + re.escape(species) + r"\\b", new_fx
    ), species

old_particle = original("client/particle/PlaceableBreakFleckParticle.java")
new_particle = current("client/particle/PlaceableBreakFleckParticle.java")
for marker in ("this.hasPhysics = true;", "this.gravity = 0.62F;",
               "this.friction = 0.76F;",
               "this.lifetime = 11 + level.random.nextInt(9);",
               "this.quadSize = 0.045F + level.random.nextFloat() * 0.045F;",
               "this.roll = level.random.nextFloat() * Mth.TWO_PI;",
               "this.oRoll = this.roll;",
               "this.xd *= 0.58D;", "this.zd *= 0.58D;",
               "this.spinSpeed *= 0.72F;",
               "float fadeStart = this.lifetime * 0.65F;"):
    assert marker in old_particle and marker in new_particle, marker
for marker in ("extends SingleQuadParticle", "SingleQuadParticle.Layer.TRANSLUCENT",
               "RandomSource random", "TextureAtlasSprite sprite",
               "sprites.get(random)", "new PlaceableBreakFleckParticle("):
    assert marker in new_particle, marker
assert "extends TextureSheetParticle" in old_particle
assert "TextureSheetParticle" not in new_particle.replace("removed TextureSheetParticle", "")
assert "getRenderType()" not in new_particle
assert "quad.setColor(color.red, color.green, color.blue);" in new_fx

mixin = current("mixin/ClientBlockBreakParticlesMixin.java")
config = json.loads((ROOT / "src/main/resources/guaniao.mixins.json").read_text("utf-8"))
assert "ClientBlockBreakParticlesMixin" in config["client"]
assert "ClientBlockBreakParticlesMixin" not in config["mixins"]
for method in ("addDestroyBlockEffect", "addBreakingBlockEffect"):
    assert f'@Inject(method = "{method}", at = @At("HEAD"), cancellable = true, require = 1)' in mixin, method
for key in ("@Mixin(ClientLevel.class)", "renderDestroy(state, level, pos,",
            "renderHit(state, level, pos, direction,",
            "Minecraft.getInstance().particleEngine",
            "callback.cancel();"):
    assert key in mixin, key

print("FABRIC 26.2 BREAK PARTICLES PARITY OK: 6 block families, original"
      " hit/colored debris formulas byte-identical, client-only effects")
