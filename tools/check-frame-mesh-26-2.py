#!/usr/bin/env python3
"""Verify the new deferred 26.2 mesh preserves every authored geometry formula."""
from pathlib import Path
import subprocess

ROOT=Path(__file__).resolve().parents[1]
BASE="src/main/java/EdDYON/guaniao/client/camera/frame/"
original=subprocess.check_output(
    ["git","show","origin/main:"+BASE+"FrameDesignRenderer.java"],
    cwd=ROOT,text=True).replace("\r\n","\n")
old_design=subprocess.check_output(
    ["git","show","origin/main:src/main/java/EdDYON/guaniao/content/camera/frame/FrameDesign.java"],
    cwd=ROOT,text=True).replace("\r\n","\n")
geom=(ROOT/(BASE+"FrameWorkbenchMeshGeometry.java")).read_text("utf-8")
submit=(ROOT/(BASE+"FrameWorkbenchMeshSubmitter.java")).read_text("utf-8")
state=(ROOT/(BASE+"FrameWorkbenchPreview3DState.java")).read_text("utf-8")
renderer=(ROOT/(BASE+"FrameWorkbenchPreview3DRenderer.java")).read_text("utf-8")
resolver=(ROOT/(BASE+"FrameWorkbenchMaterialResolver.java")).read_text("utf-8")
factory=(ROOT/(BASE+"FrameWorkbenchPreview3DFactory.java")).read_text("utf-8")
adapter=(ROOT/(BASE+"FrameWorkbenchMeshFromDesign.java")).read_text("utf-8")
client=(ROOT/"src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricClientEntrypoint.java").read_text("utf-8")
workflow=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
gradle=(ROOT/"build.gradle").read_text("utf-8")
anchor="    /** Adjacent cell edges share a plane"
old_algorithms=original[original.index(anchor):original.index("    private static void vertex(VertexConsumer")]
new_algorithms=geom[geom.index(anchor):geom.index("    public record Quad(")]
# The author did not gate exterior trim faces when trim was disabled.
# Preserve every other authored mesh/UV formula, allowing only the exact
# corrected branch already covered by the real no-trim Java regression.
expected_algorithms=old_algorithms.replace("FrameDesign.CellRect","CellRect")
expected_algorithms=expected_algorithms.replace(
    "                // Trim stops where an exterior edge becomes an inner window edge.\n"
    "                if(outside && trimStart<0)trimStart=cell;\n"
    "                if(!outside && trimStart>=0) {",
    "                // Trim stops where an exterior edge becomes an inner window edge.\n"
    "                boolean trimmed=hasTrim && outside;\n"
    "                if(trimmed && trimStart<0)trimStart=cell;\n"
    "                if(!trimmed && trimStart>=0) {")
assert "boolean trimmed=hasTrim && outside;" in expected_algorithms
assert expected_algorithms.rstrip()==new_algorithms.rstrip(), (
    "3D boundary merging, outline, face normals or tiled UV changed"
)
old_rects=old_design[old_design.index("    /** Rectangles are disjoint"):old_design.index("\n    @Nullable",old_design.index("    /** Rectangles are disjoint"))]
new_rects=geom[geom.index("    /** Rectangles are disjoint"):geom.index("    /** Adjacent cell edges share a plane")]
assert old_rects.replace("public static List<CellRect> rectangles","private static List<CellRect> rectangles").replace("GRID","32").rstrip()==new_rects.rstrip(), (
    "3D rectangle decomposition diverged from FrameDesign"
)
for token in ("outer[y]&~window[y]", "new Mesh(tile(wood),tile(rims),tile(trim)",
              "addBoundaryRuns(frame,outer,w,h,front,wood,rims,trim,hasTrim)",
              "backing.add(liningFace(b,front-0.0025F,0,w,h,glassLining))",
              "wood.add(face(b.x0,b.y0,front,b.x1,b.y1,front,0))",
              "ghostLining.add(liningFace(b,front-.0025F,0,w,h,glassLining))"):
    assert token in geom,token
for token in ("collector.submitCustomGeometry(", "sprite.getU(quad.uv()[uv])",
              "sprite.getV(quad.uv()[uv+1])", ".setNormal(snapshot,quad.nx(),quad.ny(),quad.nz())",
              "RenderTypes.entityTranslucent(", "RenderTypes.entityCutout(",
              ".setLight(packedLight)"):
    assert token in submit,token
assert ".getU(quad.uv()[uv]*16)" not in submit, "26.2 UVs are normalized"
for token in ("implements PictureInPictureRenderState", "FrameWorkbenchMeshGeometry.Mesh mesh",
              "PictureInPictureRenderState.getBounds("):
    assert token in state,token
for token in ("extends PictureInPictureRenderer<FrameWorkbenchPreview3DState>",
              "FrameWorkbenchMeshSubmitter.submit(mesh.backing()",
              "FrameWorkbenchMeshSubmitter.submit(mesh.wood()",
              "FrameWorkbenchMeshSubmitter.submit(mesh.rim()",
              "FrameWorkbenchMeshSubmitter.submit(mesh.trim()",
              "wood.red()*0.62F", "wood.green()*0.60F","wood.blue()*0.55F",
              "getTranslateY(int height, int guiScale)", "LightCoordsUtil.FULL_BRIGHT"):
    assert token in renderer,token
assert "PictureInPictureRendererRegistry.register(context -> new FrameWorkbenchPreview3DRenderer());" in client
for token in (
    "getModelManager().getBlockStateModelSet()",
    ".getParticleMaterial(state).sprite()",
    "client.getBlockColors().getTintSource(state,0)",
    "sprite.transparency().hasTranslucent()",
    "Blocks.OAK_PLANKS.defaultBlockState()",
    "item instanceof BlockItem block",
    'BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("white_wool")).defaultBlockState()',
    "paper ? 0.96F : 1.0F",
    "paper ? 0.86F : 1.0F",
    'path.equals("block/tinted_glass")',
    'block.getBlock() instanceof StainedGlassPaneBlock',
    'id.getPath().contains("glass")',
):
    assert token in resolver, token
assert "net.minecraftforge." not in resolver
for token in (
    "FrameWorkbenchMeshFromDesign.build(design,glass)",
    "FrameWorkbenchMaterialResolver.isGlass(design.liningMaterial())",
    "FrameWorkbenchMaterialResolver.surface(design.frameMaterial())",
    "FrameWorkbenchMaterialResolver.surface(design.trimMaterial())",
    "FrameWorkbenchMaterialResolver.surface(design.liningMaterial())",
    "new FrameWorkbenchPreview3DState(mesh,wood,trim,lining,",
):
    assert token in factory, token
for token in ("design.mask()", "design.windowMask()", "design.widthPixels()",
              "design.heightPixels()", "design.depthPixels()",
              "!design.trimMaterial().equals(AIR)"):
    assert token in adapter, token
assert "FrameWorkbenchMaterialResolver.java" in gradle
assert "GuaniaoFrameMeshGeometryRegression" in workflow
for cls in ("FrameWorkbenchMeshGeometry","FrameWorkbenchMeshSubmitter",
            "FrameWorkbenchPreview3DState","FrameWorkbenchPreview3DRenderer"):
    assert cls+".java" in gradle,cls
print("FRAME MESH 26.2 PARITY OK: original rectangle merge, outline, UV tiling,"
      " depth, material tint and PiP client registration")
