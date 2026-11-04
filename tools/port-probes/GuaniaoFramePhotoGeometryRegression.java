import EdDYON.guaniao.client.camera.frame.FrameWorkbenchMeshGeometry;
import EdDYON.guaniao.client.camera.frame.FrameWorkbenchPhotoGeometry;
import java.util.List;

/**
 * Pure Java regression for original photo masking, cover/contain, zoom, offsets
 * and 90-degree rotations. No Minecraft GUI or GPU required.
 */
public final class GuaniaoFramePhotoGeometryRegression {
    private GuaniaoFramePhotoGeometryRegression() { }

    private static void check(boolean ok,String why) {
        if(!ok)throw new AssertionError(why);
    }

    private static int[] rectangle(int left,int top,int right,int bottom) {
        int[] rows=new int[32];
        for(int y=top;y<bottom;y++)for(int x=left;x<right;x++)rows[y]|=1<<x;
        return rows;
    }

    private static FrameWorkbenchMeshGeometry.Mesh mesh(int[] window) {
        int[] outer=new int[32];
        java.util.Arrays.fill(outer,-1);
        return FrameWorkbenchMeshGeometry.build(outer,window,64,64,4,true,false);
    }

    private static void valid(List<FrameWorkbenchPhotoGeometry.PhotoQuad> quads) {
        for(var quad:quads) {
            check(quad.points().length==12 && quad.uv().length==8,"four photo corners");
            for(float p:quad.points())check(Float.isFinite(p),"invalid xyz");
            for(float uv:quad.uv())check(Float.isFinite(uv) && uv>=-0.00001F
                    && uv<=1.00001F,"photo UV outside image after clipping");
        }
    }

    public static void main(String[] args) {
        var noWindow=mesh(new int[32]);
        check(FrameWorkbenchPhotoGeometry.build(noWindow,0,0,1920,1080,false,100,0,0)
                .isEmpty(),"solid frame must not show a photo");

        var full=mesh(rectangle(8,8,24,24));
        var fitted=FrameWorkbenchPhotoGeometry.build(full,0,0,1,1,false,100,0,0);
        check(!fitted.isEmpty(),"photo missing from window");
        valid(fitted);
        for(var q:fitted)for(int i=2;i<12;i+=3) {
            check(Math.abs(q.points()[i]-(full.front()-0.001F))<1.0e-5F,
                    "photo must sit behind wood edge");
        }

        var contain=FrameWorkbenchPhotoGeometry.build(full,0,0,1920,1080,false,100,0,0);
        var cover=FrameWorkbenchPhotoGeometry.build(full,0,0,1920,1080,true,100,0,0);
        valid(contain);valid(cover);
        check(contain.size()==1 && cover.size()==1,"simple square window");
        float letterbox=contain.get(0).points()[1];
        float topEdge=cover.get(0).points()[1];
        check(letterbox>topEdge,"contain/cover geometry not distinguishable");

        for(int turn=0;turn<4;turn++) {
            var rotated=FrameWorkbenchPhotoGeometry.build(full,turn,0,1920,1080,true,150,10,-10);
            check(!rotated.isEmpty(),"rotated photo dropped");
            valid(rotated);
        }
        var a=FrameWorkbenchPhotoGeometry.build(full,0,1,1920,1080,true,100,0,0);
        var b=FrameWorkbenchPhotoGeometry.build(full,1,0,1920,1080,true,100,0,0);
        check(java.util.Arrays.equals(a.get(0).uv(),b.get(0).uv()),
                "extra rotation should exactly match saved rotation");

        // Two separate window islands MUST NOT become a single bounding
        // rectangle that draws through the wooden divider.
        int[] split=rectangle(3,5,10,24);
        int[] right=rectangle(21,5,29,24);
        for(int y=0;y<32;y++)split[y]|=right[y];
        var multi=FrameWorkbenchPhotoGeometry.build(mesh(split),0,0,300,300,true,100,0,0);
        check(multi.size()==2,"split photo windows merged into a rectangle");
        valid(multi);

        var zoomed=FrameWorkbenchPhotoGeometry.build(full,0,0,1200,900,true,300,0,0);
        check(!zoomed.isEmpty(),"max zoom should still show photo");
        valid(zoomed);
        check(FrameWorkbenchPhotoGeometry.build(full,0,0,1200,900,false,0,0,0).isEmpty(),
                "zero-size photo must be rejected");

        System.out.println("FRAME PHOTO 26.2 REGRESSION OK: contain, cover, offsets,"
                +" rotations, photo depth, disjoint openings, zoom and UV clipping");
    }
}
