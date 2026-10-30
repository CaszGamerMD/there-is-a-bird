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
assert old_fx[old_fx.index(visual_anchor):] == new_fx[new_fx.index(visual_anchor):], (
    "A particle color/count/motion/shape formula changed"
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
    assert "instanceof " + species in new_fx, species

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
