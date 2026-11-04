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
preview=(root/(path+"client/camera/frame/FrameWorkbenchPreview3DRenderer.java")).read_text("utf-8")
submitter=(root/(path+"client/camera/frame/FrameWorkbenchMeshSubmitter.java")).read_text("utf-8")

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
# Verify the actual 26.2 production path, not the removed 1.20.1 immediate
# renderer. Static frame preview is live; assembly animation, the visitor and
# the independent 3D progress bird still require their own deferred PiP states.
for token in ('private void extractPreview3D(', 'FrameWorkbenchPreview3DFactory.capture(',
              'FrameWorkbenchPipBridge.submit(graphics,state);'):
    assert token in screen,token
for token in ('FrameWorkbenchMeshSubmitter.submit(', 'extends PictureInPictureRenderer<FrameWorkbenchPreview3DState>',
              'collector, light'):
    assert token in preview,token
assert 'collector.submitCustomGeometry(' in submitter
for obsolete in ('FrameCraftingAnimation.renderDelivery(', 'FrameCraftingAnimation.renderObserver(',
                 'FrameDesignRenderer.render(', 'FrameCraftingProgress.render('):
    assert obsolete not in screen,obsolete
print("FRAME WORKBENCH 26.2 PIP BRIDGE OK: live deferred 3D frame preview and geometry submitter; "
      "assembly/visitor/progress-bird PiP still pending")
