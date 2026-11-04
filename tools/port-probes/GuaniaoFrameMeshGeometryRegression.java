import EdDYON.guaniao.client.camera.frame.FrameWorkbenchMeshGeometry;
import java.util.List;

/**
 * Pure-Java regression tests against the 32x32 original frame mesh geometry.
 * This compiles and runs independently from Forge, Minecraft, the GPU, or
 * GeckoLib, so the authored geometry remains testable while CI is blocked.
 */
public final class GuaniaoFrameMeshGeometryRegression {
    private GuaniaoFrameMeshGeometryRegression() { }

    private static void require(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }

    private static int[] full() {
        int[] rows = new int[32];
        java.util.Arrays.fill(rows, -1);
        return rows;
    }

    private static int[] rectangle(int left, int top, int right, int bottom) {
        int[] rows = new int[32];
        for(int y=top; y<bottom; y++) {
            for(int x=left; x<right; x++) rows[y]|=1<<x;
        }
        return rows;
    }

    private static void checkUvs(List<FrameWorkbenchMeshGeometry.Quad> quads) {
        for (var quad:quads) {
            require(quad.points().length==12 && quad.uv().length==8, "four vertices per face");
            for(float coordinate:quad.points()) require(Float.isFinite(coordinate), "invalid xyz");
            for(float uv:quad.uv()) require(Float.isFinite(uv) && uv>=-0.00001F && uv<=1.00001F, "untiled UV");
            require(Float.isFinite(quad.nx()) && Float.isFinite(quad.ny())
                    && Float.isFinite(quad.nz()), "invalid normal");
            require(Math.abs(quad.nx())+Math.abs(quad.ny())+Math.abs(quad.nz())==1,
                    "missing face normal");
        }
    }

    private static void checkAll(FrameWorkbenchMeshGeometry.Mesh mesh) {
        checkUvs(mesh.wood());
        checkUvs(mesh.rim());
        checkUvs(mesh.trim());
        checkUvs(mesh.backing());
        checkUvs(mesh.ghostLining());
        require(mesh.quadCount()>=mesh.wood().size()+mesh.backing().size(), "count mismatch");
        require(!mesh.outline().isEmpty(), "missing silhouette");
    }

    public static void main(String[] args) {
        int[] solid=full(),empty=new int[32];
        var flat=FrameWorkbenchMeshGeometry.build(solid,empty,64,64,4,true,false);
        checkAll(flat);
        require(!flat.wood().isEmpty() && !flat.trim().isEmpty(), "solid frame");
        require(flat.window().isEmpty(), "solid window should be empty");
        require(Math.abs(flat.front()-4.0F/32.0F)<1.0E-6F, "original depth scaling");

        int[] middle=rectangle(8,8,24,24);
        var cutout=FrameWorkbenchMeshGeometry.build(solid,middle,64,64,4,true,true);
        checkAll(cutout);
        require(!cutout.window().isEmpty(), "window disappeared");
        require(!cutout.ghostLining().isEmpty(), "ghost glass cutout missing");
        require(cutout.windowBounds().x0()<0 && cutout.windowBounds().x1()>0,
                "photo window X bounds");
        require(cutout.windowBounds().y0()<0 && cutout.windowBounds().y1()>0,
                "photo window Y bounds");

        // Concave mask and multiple isolated islands must not become a
        // bounding-box frame; the original design can use arbitrary masks.
        int[] irregular=rectangle(0,0,3,32);
        for(int y=28;y<32;y++) irregular[y]|=0x000FFFFF;
        irregular[5]|=1<<27;
        irregular[6]|=1<<27;
        var unusual=FrameWorkbenchMeshGeometry.build(irregular,empty,128,48,6,false,false);
        checkAll(unusual);
        require(unusual.trim().isEmpty(), "trim disabled but vertices remain");
        require(unusual.wood().size()!=flat.wood().size(), "irregular bitmap got flattened");

        int[] input=full();
        var copy=FrameWorkbenchMeshGeometry.build(input,empty,32,32,2,false,false);
        input[0]=0;
        require(copy.wood().size()>0, "mesh mutated by caller after build");

        boolean invalid=false;
        try {
            FrameWorkbenchMeshGeometry.build(empty,middle,32,32,4,false,false);
        } catch(IllegalArgumentException expected) { invalid=true; }
        require(invalid, "window outside the frame accepted");
        invalid=false;
        try {
            FrameWorkbenchMeshGeometry.build(new int[31],empty,32,32,4,false,false);
        } catch(IllegalArgumentException expected) { invalid=true; }
        require(invalid, "invalid mask length accepted");

        System.out.println("FRAME MESH 26.2 REGRESSION OK: solid, donut, irregular,"
                + " split island, 3D depth, trim, silhouette and tiled UV");
    }
}
