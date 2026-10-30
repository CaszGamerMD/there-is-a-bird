#!/usr/bin/env python3
"""Guard the real 26.2 GUI deferred-3D bridge, never a fake 2D placeholder."""
from pathlib import Path
import json

root=Path(__file__).resolve().parents[1]
path="src/main/java/EdDYON/guaniao/"
access=(root/(path+"mixin/GuaniaoGuiRenderStateAccess.java")).read_text("utf-8")
bridge=(root/(path+"client/camera/frame/FrameWorkbenchPipBridge.java")).read_text("utf-8")
probe=(root/"tools/port-probes/GuaniaoFramePictureInPictureProof.java").read_text("utf-8")
config=json.loads((root/"src/main/resources/guaniao.mixins.json").read_text("utf-8"))
gradle=(root/"build.gradle").read_text("utf-8")
client=(root/(path+"fabric/GuaniaoFabricClientEntrypoint.java")).read_text("utf-8")
screen=(root/(path+"client/camera/frame/FrameWorkbenchScreen.java")).read_text("utf-8")
renderer=(root/(path+"client/camera/frame/FrameDesignRenderer.java")).read_text("utf-8")

assert 'GuaniaoGuiRenderStateAccess' in config['client']
assert 'GuaniaoGuiRenderStateAccess' not in config['mixins']
for token in ('@Mixin(GuiGraphicsExtractor.class)', '@Accessor("guiRenderState")',
              'GuiRenderState guaniao$getGuiRenderState();'):
    assert token in access,token
for token in ('static void submit(GuiGraphicsExtractor graphics, PictureInPictureRenderState state)',
              'if (state.bounds() == null)',
              'guaniao$getGuiRenderState()',
              '.addPicturesInPictureState(state);'):
    assert token in bridge,token
assert 'RenderSystem.' not in bridge and 'graphics.pose().mulPose' not in bridge

for token in ('implements PictureInPictureRenderState', 'extends PictureInPictureRenderer<State>',
              'PictureInPictureRenderState.getBounds(',
              'PictureInPictureRendererRegistry.register(', 'submitCustomGeometry(',
              'RenderTypes.lines()', 'vertices.addVertex(snapshot,'):
    assert token in probe,token
assert 'tools/port-probes/GuaniaoFramePictureInPictureProof.java' in gradle
for source in ('GuaniaoGuiRenderStateAccess.java','FrameWorkbenchPipBridge.java',
               'FrameMaterialPalette.java'):
    assert source in gradle
# The probe is not a real renderer registration or visible artifact.
assert 'GuaniaoFramePictureInPictureProof' not in client
# Neither 3D author feature may be removed as an expedient "port".
for token in ('FrameCraftingAnimation.renderDelivery(', 'FrameCraftingAnimation.renderObserver(',
              'FrameDesignRenderer.render(', 'FrameCraftingProgress.render('):
    assert token in screen,token
assert 'public static void renderWorkshop(' in renderer
print("FRAME WORKBENCH 26.2 PIP BRIDGE OK: client-only render-state accessor,"
      " deferred 3D submission and isolated geometry API proof; preview unfinished")
