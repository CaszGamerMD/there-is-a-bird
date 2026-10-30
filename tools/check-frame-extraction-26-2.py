#!/usr/bin/env python3
"""Guard 26.2 Frame Workbench extraction, input safety and crafting UI parity."""
from pathlib import Path
import subprocess

ROOT=Path(__file__).resolve().parents[1]
BASE="src/main/java/EdDYON/guaniao/client/camera/frame/"
old=subprocess.check_output(["git","show","origin/main:"+BASE+"FrameWorkbenchScreen.java"],
                            cwd=ROOT,text=True).replace("\r\n","\n")
new=(ROOT/(BASE+"FrameWorkbenchScreen.java")).read_text("utf-8").replace("\r\n","\n")
old_food=subprocess.check_output(["git","show","origin/main:"+BASE+"FrameCraftingProgress.java"],
                                 cwd=ROOT,text=True).replace("\r\n","\n")
food=(ROOT/(BASE+"FrameCraftingProgress2D.java")).read_text("utf-8")

for marker in (
    "@Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick)",
    "@Override public void extractContents(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick)",
    "@Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY)",
    "graphics.pose().translate(this.originX,this.originY);",
    "graphics.pose().scale(this.uiScale,this.uiScale);",
    "super.extractContents(graphics,localX,localY,partialTick);",
    "graphics.pose().popMatrix();",
    "this.hoveredSlot=null;",
    "this.extractModalPanel(graphics,localX,localY);",
    "this.extractWorkbenchBackdrop(graphics,localX,localY);",
    "renderable.extractRenderState(graphics,localX,localY,partialTick);",
    "FrameWorkbenchPreview3DFactory.capture(this.design,",
    "FrameWorkbenchPipBridge.submit(graphics,state);",
    "PhotographData.hasImage(this.previewStack))return;",
    "FrameCraftingProgress2D.extract(graphics,",
    "FrameWorkshopBird.progressFood(this.activeWorkSeed)",
    "FrameWorkshopBird.progressFood(this.progressBirdClock.seed())",
    "graphics.setTooltipForNextFrame(this.font,this.paletteEntries.get(index),mouseX,mouseY)",
    "graphics.setTooltipForNextFrame(this.font,widget.getMessage(),mouseX,mouseY)",
    "this.outlineEditor.render(graphics,mouseX,mouseY)",
    "graphics.item(this.paletteEntries.get(start+cell),x,y)",
    "this.frameWorkTicks=this.workshopClock.render(System.nanoTime(),this.minecraft.isPaused())",
    "this.progressBirdClock.complete(this.menu.getWorkSeed(),System.nanoTime(),this.minecraft.isPaused())",
    "graphics.outline(331,230,30,30,(alpha<<24)|0xFFF0B8);",
    "new MouseButtonEvent(this.localX(mouseX),this.localY(mouseY),event.buttonInfo())"
):
    assert marker in new,marker
for marker in (
    "PANEL_WIDTH=390,PANEL_HEIGHT=300", "CANVAS_X=92,CANVAS_Y=52,CELL=4",
    "PALETTE_COLUMNS=14,PALETTE_ROWS=10",
    "FrameWorkbenchBackdrop.render(g,8,37,63,161);",
    "FrameWorkbenchBackdrop.render(g,241,203,138,71);",
    "slot(g,18,54);",
    "slot(g,FrameWorkbenchMenu.OUTPUT_X,FrameWorkbenchMenu.OUTPUT_Y);",
    "slot(g,18+col*18,212+row*18);",
    "slot(g,18+col*18,270);",
    "FrameDesign.erode(mask,this.design.borderCells())",
    "this.outlineConfirm.active=this.outlineEditor.valid();",
    'text("seconds_remaining",',
    '"work_stage."+stage',
    "this.readyFlashTicks>0"):
    assert marker in old and marker in new,marker

for marker in (
    "FrameCraftingMeadow.render(graphics);",
    "0.7F*foodScale,0.7F*foodScale",
    "graphics.item(food,-8,-12);",
    "graphics.fill(312-spread,258-lift,314-spread,260-lift,0xFFC7B783);",
    "graphics.fill(316+spread,259-lift,317+spread,260-lift,0xFFE2D5AE);"):
    assert marker in food,marker
for original in (
    "float foodScale=eatingTicks<0?1:1-Mth.clamp((eatingTicks-8)/16,0,1);",
    "int spread=Math.round(bite*6),lift=Math.round((float)Math.sin(bite*Math.PI)*4);",
    "graphics.renderItem(FrameWorkshopBird.progressFood(seed),-8,-12);"):
    assert original in old_food,original
assert "net.minecraftforge." not in food
assert "GuiGraphics graphics" not in food
assert "FrameCraftingProgress2D.java" in (ROOT/"build.gradle").read_text("utf-8")
# Don't quietly discard any original 3D authored content to get source checks green.
for original in ("FrameCraftingAnimation.renderObserver(",
                 "FrameCraftingAnimation.renderDelivery(",
                 "FrameCraftingAnimation.renderPreview(",
                 "FrameCraftingProgress.render(",
                 "FrameDesignRenderer.render("):
    assert original in new,original
print("FRAME WORKBENCH EXTRACTOR PARITY OK: scaled slot/native controls, masked"
      " 32x32 canvas, 140-cell modal, no click-through, tooltip coordinates,"
      " 2D crafting clock/food and original 3D features still retained")
