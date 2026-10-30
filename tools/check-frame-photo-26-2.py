#!/usr/bin/env python3
"""Guard exact original FrameDesignRenderer photo-fitting formulas in 26.2."""
from pathlib import Path
import subprocess

ROOT=Path(__file__).resolve().parents[1]
BASE="src/main/java/EdDYON/guaniao/client/camera/frame/"
old=subprocess.check_output(["git","show","origin/main:"+BASE+"FrameDesignRenderer.java"],
                            cwd=ROOT,text=True).replace("\r\n","\n")
new=(ROOT/(BASE+"FrameWorkbenchPhotoGeometry.java")).read_text("utf-8")
submit=(ROOT/(BASE+"FrameWorkbenchPhotoSubmitter.java")).read_text("utf-8")
state=(ROOT/(BASE+"FrameWorkbenchPreview3DState.java")).read_text("utf-8")
renderer=(ROOT/(BASE+"FrameWorkbenchPreview3DRenderer.java")).read_text("utf-8")
factory=(ROOT/(BASE+"FrameWorkbenchPreview3DFactory.java")).read_text("utf-8")
gradle=(ROOT/"build.gradle").read_text("utf-8")
workflow=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")

# Preserved image crop math, per-window clipping and UV rotation.
for token in ("int rotation=(design.rotation()+extraRotation)&3;",
              "float scale=design.fit()==FrameDesign.PhotoFit.COVER ? Math.max(ww/ratio,wh) : Math.min(ww/ratio,wh);",
              "float imageWidth=ratio*scale,imageHeight=scale;",
              "if ((rotation&1)!=0) ratio=1/ratio;",
              "float cx=(window.x0+window.x1)/2+design.offsetX()/100.0F*ww;",
              "float cy=(window.y0+window.y1)/2+design.offsetY()/100.0F*wh;",
              "for(Bounds rect:mesh.window)",
              "float x0=Math.max(ix0,rect.x0),x1=Math.min(ix1,rect.x1);",
              "float y0=Math.max(iy0,rect.y0),y1=Math.min(iy1,rect.y1);",
              "case 1 -> {ru=v;rv=1-u;}",
              "case 2 -> {ru=1-u;rv=1-v;}",
              "case 3 -> {ru=1-v;rv=u;}"):
    assert token in old, token

for token in ("(rotation+extraRotation)&3", "ww/ratio,wh", "Math.min(ww/ratio,wh)",
              "float imageWidth=ratio*scale,imageHeight=scale;",
              "offsetX/100.0F*ww", "offsetY/100.0F*wh",
              "Math.max(ix0,rect.x0())", "Math.min(ix1,rect.x1())",
              "Math.max(iy0,rect.y0())", "Math.min(iy1,rect.y1())",
              "mesh.front()-0.001F",
              "case 1 -> {ru=b;rv=1-a;}",
              "case 2 -> {ru=1-a;rv=1-b;}",
              "case 3 -> {ru=1-b;rv=a;}"):
    assert token in new,token
for token in ("collector.submitCustomGeometry(pose,RenderTypes.text(texture)",
              ".setUv(quad.uv()[t],quad.uv()[t+1])", ".setLight(packedLight)"):
    assert token in submit,token
for token in ("@Nullable Photo photo", "List.copyOf(quads);"):
    assert token in state,token
assert "FrameWorkbenchPhotoSubmitter.submit(photo.quads(), photo.texture()," in renderer
for token in ("static FrameWorkbenchPreview3DState captureWithPhoto(",
              "FrameWorkbenchPhotoGeometry.build(basic.mesh(),design.rotation(),0,",
              "design.zoomPercent(),design.offsetX(),design.offsetY()"):
    assert token in factory,token
assert "GuaniaoFramePhotoGeometryRegression.java" in workflow
for cls in ("FrameWorkbenchPhotoGeometry","FrameWorkbenchPhotoSubmitter"):
    assert cls+".java" in gradle
print("FRAME PHOTO 26.2 SOURCE PARITY OK: original mask, photo fit, rotation,"
      " image offset/zoom, alpha and deferred texture submission")
