package EdDYON.guaniao.client.camera.frame;

import EdDYON.guaniao.content.camera.frame.FrameDesign;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jspecify.annotations.Nullable;

/**
 * Produces the actual immutable 3D Frame Workbench render state from
 * a user-edited design and its live baked material sprites.
 *
 * The screen calls capture() during GUI extraction and submits the result
 * through FrameWorkbenchPipBridge. The renderer receives only immutable
 * design geometry, texture snapshots, angles, and screen placement.
 */
final class FrameWorkbenchPreview3DFactory {
    private FrameWorkbenchPreview3DFactory() { }

    static FrameWorkbenchPreview3DState capture(FrameDesign design, float yaw, float pitch,
            int x0,int y0,int x1,int y1,float scale,@Nullable ScreenRectangle scissor) {
        boolean glass=FrameWorkbenchMaterialResolver.isGlass(design.liningMaterial());
        var mesh=FrameWorkbenchMeshFromDesign.build(design,glass);
        var wood=FrameWorkbenchMaterialResolver.surface(design.frameMaterial());
        var lining=FrameWorkbenchMaterialResolver.surface(design.liningMaterial());
        var trim=design.trimMaterial().equals(net.minecraft.resources.Identifier.withDefaultNamespace("air"))
                ? null : FrameWorkbenchMaterialResolver.surface(design.trimMaterial());
        return new FrameWorkbenchPreview3DState(mesh,wood,trim,lining,
                yaw,pitch,x0,y0,x1,y1,scale,scissor);
    }
}
