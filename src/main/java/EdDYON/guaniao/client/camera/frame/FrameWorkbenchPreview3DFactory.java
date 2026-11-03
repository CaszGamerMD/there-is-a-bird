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
    /**
     * The caller supplies the real downloaded photograph's Identifier and
     * pixel dimensions after its 26.2 texture cache has been migrated.
     * Null means the film has no image; it does NOT synthesize fake artwork.
     */
    static FrameWorkbenchPreview3DState captureWithPhoto(FrameDesign design,
            float yaw,float pitch,int x0,int y0,int x1,int y1,float scale,
            @Nullable ScreenRectangle scissor,net.minecraft.resources.@Nullable Identifier texture,
            int imageWidth,int imageHeight) {
        var basic=capture(design,yaw,pitch,x0,y0,x1,y1,scale,scissor);
        if(texture==null || imageWidth<=0 || imageHeight<=0) return basic;
        var fitted=FrameWorkbenchPhotoGeometry.build(basic.mesh(),design.rotation(),0,
                imageWidth,imageHeight,design.fit()==FrameDesign.PhotoFit.COVER,
                design.zoomPercent(),design.offsetX(),design.offsetY());
        if(fitted.isEmpty())return basic;
        var photo=new FrameWorkbenchPreview3DState.Photo(texture,fitted,1.0F);
        return new FrameWorkbenchPreview3DState(basic.mesh(),basic.wood(),basic.trim(),
                basic.lining(),photo,yaw,pitch,x0,y0,x1,y1,scale,scissor);
    }

}
