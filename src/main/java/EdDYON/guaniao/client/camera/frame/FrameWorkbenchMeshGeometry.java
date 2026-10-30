package EdDYON.guaniao.client.camera.frame;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure, thread-safe 3D mesh generation for the Minecraft 26.2 Frame Workbench.
 *
 * The faces, silhouette runs, merging, normals, and tiled UV generation are
 * copied from the authored FrameDesignRenderer and FrameDesign rectangles().
 * This deliberately accepts immutable input values rather than old 1.20.1
 * frame registries/render objects, so it can be compiled and tested before
 * the deferred rendering and photograph/material APIs are completely ported.
 *
 * A face contains four XYZ vertices, four UV pairs and the original normal.
 * Units: frame dimensions/depth in original pixels; masks are 32 bitmap rows.
 */
public final class FrameWorkbenchMeshGeometry {
    private FrameWorkbenchMeshGeometry() { }

    public static Mesh build(int[] mask, int[] windowMask, int widthPixels,
                             int heightPixels, int depthPixels,
                             boolean hasTrim, boolean glassLining) {
        if (mask == null || windowMask == null || mask.length != 32 || windowMask.length != 32)
            throw new IllegalArgumentException("A frame mask must contain 32 rows");
        if (widthPixels <= 0 || heightPixels <= 0 || depthPixels < 0)
            throw new IllegalArgumentException("Frame dimensions must be positive");
        int[] outer=mask.clone(),window=windowMask.clone(),frame=new int[32];
        for(int y=0;y<32;y++) {
            if ((window[y] & ~outer[y]) != 0)
                throw new IllegalArgumentException("A photo window must stay inside its frame mask");
            frame[y]=outer[y]&~window[y];
        }
        float w=widthPixels/16.0F,h=heightPixels/16.0F,front=depthPixels/32.0F;
        List<Quad> wood=new ArrayList<>(),rims=new ArrayList<>(),trim=new ArrayList<>();
        List<Bounds> windows=new ArrayList<>();
        List<Quad> backing=new ArrayList<>(),ghostLining=new ArrayList<>();
        for(CellRect rect:rectangles(outer)) {
            Bounds b=bounds(rect,w,h);
            backing.add(liningFace(b,front-0.0025F,0,w,h,glassLining));
            backing.add(liningFace(b,-front+0.0005F,1,w,h,glassLining));
        }
        for(CellRect rect:rectangles(frame)) {
            Bounds b=bounds(rect,w,h);
            wood.add(face(b.x0,b.y0,front,b.x1,b.y1,front,0));
            wood.add(face(b.x0,b.y0,-front,b.x1,b.y1,-front,1));
        }
        float minX=Float.POSITIVE_INFINITY,minY=Float.POSITIVE_INFINITY,maxX=-Float.POSITIVE_INFINITY,maxY=-Float.POSITIVE_INFINITY;
        for(CellRect rect:rectangles(window)) {
            Bounds b=bounds(rect,w,h);
            windows.add(b);
            ghostLining.add(liningFace(b,front-.0025F,0,w,h,glassLining));
            minX=Math.min(minX,b.x0);minY=Math.min(minY,b.y0);maxX=Math.max(maxX,b.x1);maxY=Math.max(maxY,b.y1);
        }
        addBoundaryRuns(frame,outer,w,h,front,wood,rims,trim,hasTrim);
        Bounds windowBounds=windows.isEmpty() ? new Bounds(0,0,0,0) : new Bounds(minX,minY,maxX,maxY);
        return new Mesh(tile(wood),tile(rims),tile(trim),List.copyOf(windows),
                tile(backing),tile(ghostLining),outline(outer,w,h),windowBounds,front);
    }

    /** Rectangles are disjoint, in bitmap coordinates; row zero is the top edge. */
    private static List<CellRect> rectangles(int[] rows) {
        if (rows == null || rows.length != 32) throw new IllegalArgumentException("Invalid mask");
        List<CellRect> result = new ArrayList<>();
        int[] remaining = rows.clone();
        for (int y = 0; y < 32; y++) {
            while (remaining[y] != 0) {
                int left = Integer.numberOfTrailingZeros(remaining[y]), right = left + 1;
                while (right < 32 && (remaining[y] & (1 << right)) != 0) right++;
                int bits = (int)(((1L << (right - left)) - 1L) << left);
                int bottom = y + 1;
                while (bottom < 32 && (remaining[bottom] & bits) == bits) bottom++;
                for (int row = y; row < bottom; row++) remaining[row] &= ~bits;
                result.add(new CellRect(left, y, right, bottom));
            }
        }
        return List.copyOf(result);
    }

    public record CellRect(int left, int top, int right, int bottom) { }

    /** Adjacent cell edges share a plane, normal and linear UVs; only their internal seams disappear. */
    private static void addBoundaryRuns(int[] frame,int[] outer,float w,float h,float front,
                                        List<Quad> wood,List<Quad> rims,List<Quad> trim,boolean hasTrim) {
        float thickness=Math.min(w,h)/32*0.20F;
        for(int side=2;side<=5;side++) for(int line=0;line<32;line++) {
            int start=-1,rimStart=-1,trimStart=-1;
            for(int cell=0;cell<=32;cell++) {
                int x=side<4 ? line : cell,y=side<4 ? cell : line;
                int nx=x+(side==2 ? -1 : side==3 ? 1 : 0),ny=y+(side==4 ? 1 : side==5 ? -1 : 0);
                boolean edge=cell<32 && occupied(frame,x,y) && !occupied(frame,nx,ny);
                boolean outside=edge && !occupied(outer,nx,ny);
                // The trim owns an exterior side. Two coplanar materials flicker and blend twice.
                boolean boundary=edge && (!hasTrim || !outside);
                if(boundary && start<0)start=cell;
                if(!boundary && start>=0) {
                    Bounds b=boundaryBounds(side,line,start,cell,w,h);
                    wood.add(face(b.x0,b.y0,-front,b.x1,b.y1,front,side));
                    start=-1;
                }
                if(edge && rimStart<0)rimStart=cell;
                if(!edge && rimStart>=0) {
                    Bounds b=boundaryBounds(side,line,rimStart,cell,w,h);
                    Bounds rim=boundaryRim(b,side,thickness);
                    rims.add(face(rim.x0,rim.y0,front+.0005F,rim.x1,rim.y1,front+.0005F,0));
                    rimStart=-1;
                }
                // Trim stops where an exterior edge becomes an inner window edge.
                if(outside && trimStart<0)trimStart=cell;
                if(!outside && trimStart>=0) {
                    Bounds b=boundaryBounds(side,line,trimStart,cell,w,h);
                    Bounds rim=boundaryRim(b,side,thickness);
                    trim.add(face(rim.x0,rim.y0,front+.001F,rim.x1,rim.y1,front+.001F,0));
                    trim.add(face(b.x0,b.y0,-front,b.x1,b.y1,front,side));
                    trimStart=-1;
                }
            }
        }
    }

    private static Bounds boundaryBounds(int side,int line,int start,int end,float w,float h) {
        return bounds(side<4 ? new CellRect(line,start,line+1,end)
                : new CellRect(start,line,end,line+1),w,h);
    }

    private static Bounds boundaryRim(Bounds b,int side,float thickness) {
        return switch(side) {
            case 2 -> new Bounds(b.x0,b.y0,b.x0+thickness,b.y1);
            case 3 -> new Bounds(b.x1-thickness,b.y0,b.x1,b.y1);
            case 4 -> new Bounds(b.x0,b.y0,b.x1,b.y0+thickness);
            default -> new Bounds(b.x0,b.y1-thickness,b.x1,b.y1);
        };
    }

    /** Merge straight runs on the actual mask, including holes, rather than outlining its bounding box. */
    private static List<OutlineEdge> outline(int[] mask,float width,float height) {
        List<OutlineEdge> edges=new ArrayList<>();
        for(int y=0;y<=32;y++) {
            int start=-1;
            for(int x=0;x<=32;x++) {
                boolean boundary=x<32 && occupied(mask,x,y-1)!=occupied(mask,x,y);
                if(boundary && start<0)start=x;
                if(!boundary && start>=0) {
                    float py=(.5F-y/32F)*height;
                    edges.add(new OutlineEdge((start/32F-.5F)*width,py,(x/32F-.5F)*width,py));
                    start=-1;
                }
            }
        }
        for(int x=0;x<=32;x++) {
            int start=-1;
            for(int y=0;y<=32;y++) {
                boolean boundary=y<32 && occupied(mask,x-1,y)!=occupied(mask,x,y);
                if(boundary && start<0)start=y;
                if(!boundary && start>=0) {
                    float px=(x/32F-.5F)*width;
                    edges.add(new OutlineEdge(px,(.5F-start/32F)*height,px,(.5F-y/32F)*height));
                    start=-1;
                }
            }
        }
        return List.copyOf(edges);
    }

    private static boolean occupied(int[] mask,int x,int y) {return x>=0 && y>=0 && x<32 && y<32 && (mask[y]&(1<<x))!=0;}
    private static Bounds bounds(CellRect r,float w,float h) {
        return new Bounds((r.left()/32.0F-0.5F)*w,(0.5F-r.bottom()/32.0F)*h,
                (r.right()/32.0F-0.5F)*w,(0.5F-r.top()/32.0F)*h);
    }

    private static Quad face(float x0,float y0,float z0,float x1,float y1,float z1,int side) {
        float[] p;float nx=0,ny=0,nz=0;
        switch(side) {
            case 0 -> {p=new float[]{x0,y0,z1,x1,y0,z1,x1,y1,z1,x0,y1,z1};nz=1;}
            case 1 -> {p=new float[]{x1,y0,z0,x0,y0,z0,x0,y1,z0,x1,y1,z0};nz=-1;}
            case 2 -> {p=new float[]{x0,y0,z0,x0,y0,z1,x0,y1,z1,x0,y1,z0};nx=-1;}
            case 3 -> {p=new float[]{x1,y0,z1,x1,y0,z0,x1,y1,z0,x1,y1,z1};nx=1;}
            case 4 -> {p=new float[]{x0,y0,z0,x1,y0,z0,x1,y0,z1,x0,y0,z1};ny=-1;}
            default -> {p=new float[]{x0,y1,z1,x1,y1,z1,x1,y1,z0,x0,y1,z0};ny=1;}
        }
        float[] uv=new float[8];
        for(int i=0;i<4;i++) {uv[i*2]=side==2 || side==3 ? p[i*3+2] : p[i*3];uv[i*2+1]=side>=4 ? p[i*3+2] : -p[i*3+1];}
        return new Quad(p,uv,nx,ny,nz);
    }

    private static Quad liningFace(Bounds bounds,float z,int side,float width,float height,boolean glass) {
        Quad face=face(bounds.x0,bounds.y0,z,bounds.x1,bounds.y1,z,side);
        if(!glass)return face;
        float[] uv=new float[8];
        for(int i=0;i<4;i++) {
            uv[i*2]=face.points[i*3]/width+.5F;
            uv[i*2+1]=.5F-face.points[i*3+1]/height;
        }
        return new Quad(face.points,uv,face.nx,face.ny,face.nz);
    }

    /** Split only when building geometry: atlas sprites cannot use GL_REPEAT. */
    private static List<Quad> tile(List<Quad> faces) {
        List<Quad> result=new ArrayList<>();
        for (Quad face:faces) {
            float u0=face.uv[0],u1=face.uv[2],v0=face.uv[1],v1=face.uv[7];
            if (Math.abs(u1-u0)<0.000001F || Math.abs(v1-v0)<0.000001F) continue;
            float minU=Math.min(u0,u1),maxU=Math.max(u0,u1),minV=Math.min(v0,v1),maxV=Math.max(v0,v1);
            for(int tu=(int)Math.floor(minU);tu<Math.ceil(maxU);tu++) {
                for(int tv=(int)Math.floor(minV);tv<Math.ceil(maxV);tv++) {
                    float left=Math.max(minU,tu),right=Math.min(maxU,tu+1);
                    float top=Math.max(minV,tv),bottom=Math.min(maxV,tv+1);
                    float ua=u0<u1 ? left : right,ub=u0<u1 ? right : left;
                    float va=v0<v1 ? top : bottom,vb=v0<v1 ? bottom : top;
                    float[] uv={ua,va,ub,va,ub,vb,ua,vb},points=new float[12];
                    for(int i=0;i<4;i++) {
                        float du=(uv[i*2]-u0)/(u1-u0),dv=(uv[i*2+1]-v0)/(v1-v0);
                        for(int axis=0;axis<3;axis++) points[i*3+axis]=face.points[axis]
                                +(face.points[3+axis]-face.points[axis])*du+(face.points[9+axis]-face.points[axis])*dv;
                        uv[i*2]-=tu;uv[i*2+1]-=tv;
                    }
                    result.add(new Quad(points,uv,face.nx,face.ny,face.nz));
                }
            }
        }
        return List.copyOf(result);
    }


    public record Quad(float[] points,float[] uv,float nx,float ny,float nz) { }
    public record OutlineEdge(float x0,float y0,float x1,float y1) { }
    public record Bounds(float x0,float y0,float x1,float y1) { }
    public record Mesh(List<Quad> wood,List<Quad> rim,List<Quad> trim,List<Bounds> window,
                       List<Quad> backing,List<Quad> ghostLining,List<OutlineEdge> outline,
                       Bounds windowBounds,float front) {
        public int quadCount() {
            return wood.size()+rim.size()+trim.size()+backing.size()+ghostLining.size()+outline.size();
        }
    }
}
