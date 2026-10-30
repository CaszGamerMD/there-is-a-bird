package EdDYON.guaniao.client.camera.frame;

import EdDYON.guaniao.content.camera.frame.FrameDesign;
import net.minecraft.resources.Identifier;

/**
 * Converts the original persisted FrameDesign bitmap and dimensions into
 * the exact authored surface mesh for the 26.2 deferred 3D preview.
 *
 * The caller resolves whether the chosen lining is glass from its real
 * model/sprite. That information belongs to material resolution, not mask
 * geometry. No frame is flattened or replaced with a rectangular placeholder.
 */
final class FrameWorkbenchMeshFromDesign {
    private static final Identifier AIR=Identifier.withDefaultNamespace("air");

    private FrameWorkbenchMeshFromDesign() { }

    static FrameWorkbenchMeshGeometry.Mesh build(FrameDesign design, boolean glassLining) {
        return FrameWorkbenchMeshGeometry.build(
                design.mask(), design.windowMask(),
                design.widthPixels(), design.heightPixels(), design.depthPixels(),
                !design.trimMaterial().equals(AIR), glassLining);
    }
}
