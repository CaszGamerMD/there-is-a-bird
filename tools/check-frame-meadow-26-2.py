#!/usr/bin/env python3
"""Keep the second progress bird's original resource-pack-aware meadow geometry."""
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[1]
BASE="src/main/java/EdDYON/guaniao/client/camera/frame/"
old=subprocess.check_output(["git","show","origin/main:"+BASE+"FrameCraftingProgress.java"],
                            cwd=ROOT,text=True).replace("\r\n","\n")
new=(ROOT/(BASE+"FrameCraftingMeadow.java")).read_text("utf-8")
screen=(ROOT/(BASE+"FrameWorkbenchScreen.java")).read_text("utf-8")
progress=(ROOT/(BASE+"FrameCraftingProgress.java")).read_text("utf-8")
for sprite in ("block/grass_block_side","block/grass_block_side_overlay",
               "block/cornflower","block/dandelion","block/grass"):
    assert sprite in old and sprite in new,sprite
for marker in ("for (int x = 247; x < 324; x += 7)",
               "CORNFLOWER, 251, 7","GRASS, 270, 5",
               "DANDELION, 284, 6","GRASS, 299, 5"):
    assert marker in new,marker
for n in ("247","324","251","270","284","299"):
    assert n in old,n
for marker in ("getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)",
               "BlockTintSource tintSource",
               "client.getBlockColors().getTintSource(state, 0)",
               "tintSource.color(state)", "graphics.blitSprite(RenderPipelines.GUI_TEXTURED"):
    assert marker in new,marker
for obsolete in ("GuiGraphics;", "getTextureAtlas(", "getBlockColors().getColor(",
                 "net.minecraftforge.", "graphics.blit("):
    assert obsolete not in new,obsolete
# Preserve food and GeckoLib progress bird in the original rendering caller;
# the meadow helper is staged pending the shared 3D PiP integration.
assert "FrameWorkshopBird.renderProgress(" in progress
assert "FrameWorkshopBird.progressFood(seed)" in progress
assert "FrameCraftingProgress.render(" in screen
assert "FrameCraftingMeadow.java" in (ROOT/"build.gradle").read_text("utf-8")
print("FRAME CRAFTING MEADOW 26.2 PARITY OK: 11 grass tiles, four plants,"
      " original positions and inventory-tinted resource-pack sprites")
