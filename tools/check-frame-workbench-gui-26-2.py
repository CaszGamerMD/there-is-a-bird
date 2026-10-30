#!/usr/bin/env python3
"""Partial 26.2 Frame Workbench GUI conversion: keep author logic, not just skin."""
from pathlib import Path
import subprocess

ROOT=Path(__file__).resolve().parents[1]
BASE="src/main/java/EdDYON/guaniao/client/camera/frame/"

def old(name):
    return subprocess.check_output(
        ["git", "show", "origin/main:"+BASE+name+".java"],cwd=ROOT,text=True
    ).replace("\r\n","\n")

def new(name):
    return (ROOT/(BASE+name+".java")).read_text("utf-8").replace("\r\n","\n")

editor=new("FrameOutlineEditor")
legacy=old("FrameOutlineEditor")
expected=legacy.replace("import net.minecraft.client.gui.GuiGraphics;",
                        "import net.minecraft.client.gui.GuiGraphicsExtractor;")
expected=expected.replace("void render(GuiGraphics graphics,int mouseX,int mouseY)",
                          "void render(GuiGraphicsExtractor graphics,int mouseX,int mouseY)")
expected=expected.replace("graphics.renderOutline(", "graphics.outline(")
assert expected==editor, "Outline editing/pan/zoom/flood-fill changed beyond 26.2 drawing API"

backdrop=new("FrameWorkbenchBackdrop")
assert 'Identifier.withDefaultNamespace("textures/block/birch_planks.png")' in backdrop
assert "TILE_SIZE=38" in backdrop
assert "SHADE=0.48F" in backdrop
assert "0xFF7A7A7A" in backdrop
for token in ("graphics.fillGradient(x,y,x+width,y+height,0x08000000,0x28000000);",
              "graphics.fill(x,y,x+width,y+1,0x48000000);",
              "graphics.fill(x,y,x+1,y+height,0x48000000);"):
    assert token in old("FrameWorkbenchBackdrop") and token in backdrop,token
assert "graphics.setColor(" not in backdrop
assert "RenderPipelines.GUI_TEXTURED" in backdrop

widget=new("FrameNumberControl")
original_widget=old("FrameNumberControl")
for token in ("field.setFilter(value->value.matches(",
              "BigDecimal.valueOf(value).divide(BigDecimal.valueOf(divisor))",
              "this.getter.getAsInt()+direction*this.step.getAsInt()",
              "this.setter.accept((int)Math.round(number))",
              "this.slider=new ValueSlider(x+20,y+16,width-40)",
              "this.field.setFocused(false);",
              "boolean isEditing()", "boolean isSliding()"):
    assert token in original_widget and token in widget,token
for token in ("extractWidgetRenderState(GuiGraphicsExtractor graphics",
              "this.field.extractRenderState(", "this.slider.extractRenderState(",
              "mouseClicked(MouseButtonEvent event,boolean doubleClick)",
              "mouseDragged(MouseButtonEvent event,double dx,double dy)",
              "mouseReleased(MouseButtonEvent event)", "keyPressed(KeyEvent event)",
              "charTyped(CharacterEvent event)", "super(x,y,width,16,Component.empty(),0)",
              "protected void updateMessage()", "protected void applyValue()"):
    assert token in widget,token
assert "renderWidget(" not in widget
assert "mouseClicked(double " not in widget
assert "SLIDER_LOCATION" not in widget

screen=new("FrameWorkbenchScreen")
old_screen=old("FrameWorkbenchScreen")
for token in ("this.paintCell(this.lastCellX,this.lastCellY);",
              "this.design=this.design.withDecorations(pieces);",
              "this.viewPitch=Mth.clamp(", "this.palettePage=Mth.clamp(",
              "this.observerClock.complete(", "FrameDesignRenderer.render(",
              "FrameCraftingAnimation.renderObserver("):
    assert token in old_screen and token in screen,token
for token in ("mouseClicked(MouseButtonEvent event,boolean doubleClick)",
              "mouseDragged(MouseButtonEvent event,double dragX,double dragY)",
              "mouseReleased(MouseButtonEvent event)",
              "mouseScrolled(double mouseX,double mouseY,double scrollX,double scrollY)",
              "keyPressed(KeyEvent event)",
              "charTyped(CharacterEvent event)",
              "super.mouseClicked(new MouseButtonEvent(x,y,event.buttonInfo()),doubleClick)",
              "new MouseButtonEvent(this.localX(mouseX),this.localY(mouseY),event.buttonInfo())",
              "this.minecraft.options.keyInventory.matches(event)",
              "Button.Plain(x,y,width,height,message",
              "extractContents(net.minecraft.client.gui.GuiGraphicsExtractor graphics",
              "this.extractDefaultSprite(graphics)"):
    assert token in screen,token
assert "super(menu,inventory,title,PANEL_WIDTH,PANEL_HEIGHT);" in screen
assert "this.imageWidth=PANEL_WIDTH;" not in screen
assert "this.imageHeight=PANEL_HEIGHT;" not in screen
for obsolete in ("mouseClicked(double mouseX,double mouseY,int button)",
                 "mouseDragged(double mouseX,double mouseY,int button",
                 "mouseReleased(double mouseX,double mouseY,int button)",
                 "keyPressed(int key,int scan,int modifiers)",
                 "renderString(GuiGraphics graphics"):
    assert obsolete not in screen,obsolete

# The 3D preview / crafting-bird renderer MUST still exist. This guard is
# deliberately NOT a full compile claim; the remaining old GUI method
# signatures are staged for the next conversion without dropping visuals.
assert "FrameCraftingAnimation.renderDelivery(" in screen
assert "FrameCraftingProgress.render(" in screen
assert "FrameDesignRenderer.renderWorkshop(" in new("FrameCraftingAnimation")
assert "FrameDesignRenderer.render(" in new("FrameCraftingAnimation")
print("FRAME WORKBENCH 26.2 PARTIAL GUI PARITY OK: controls, outline editor,"
      " button labels and scaled pointer dispatch; 3D render migration remains")
