#!/usr/bin/env python3
"""Keep the extracted 26.2 crafting meadow and seeded food presentation."""
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
# The real 26.2 path submits the meadow and deterministic food through the
# extracted GUI lifecycle. The legacy immediate-mode GeckoLib bird call is
# intentionally NOT counted as migrated; its dedicated 3D PiP work remains pending.
assert "FrameCraftingProgress2D.extract(graphics, food, eatingTicks)" in progress
assert "FrameCraftingProgress2D.extract(graphics," in screen
assert "FrameWorkshopBird.progressFood(this.activeWorkSeed)" in screen
assert "FrameWorkshopBird.progressFood(this.progressBirdClock.seed())" in screen
assert "FrameWorkshopBird.renderProgress(" not in progress
assert "FrameCraftingProgress.render(" not in screen
for obsolete in ("import net.minecraft.client.gui.GuiGraphics;", "renderBg(", "void render(",
                 "FrameCraftingAnimation.renderPreview(", "FrameCraftingAnimation.renderObserver("):
    assert obsolete not in screen, obsolete
print("FRAME CRAFTING MEADOW 26.2 PARITY OK: 11 grass tiles, four plants,"
      " original positions and inventory-tinted resource-pack sprites; 3D progress bird pending PiP")
