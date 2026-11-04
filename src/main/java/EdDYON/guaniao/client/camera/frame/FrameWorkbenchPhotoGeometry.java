package EdDYON.guaniao.client.camera.frame;

import java.util.ArrayList;
import java.util.List;

/**
 * Original photograph-fit math extracted from FrameDesignRenderer.renderPhoto.
 * Pure Java; geometry can be tested without the client, GPU or photo texture.
 *
 * A photo is clipped to each true 32x32 window rectangle, not the full frame
 * bounding box. It retains contain/cover, zoom, x/y offset and 90-degree
 * rotation. The resulting quad UVs are normalized 0..1 and unmodified by
 * the 26.2 frame-mesh atlas conversion.
 */
public final class FrameWorkbenchPhotoGeometry {
    private FrameWorkbenchPhotoGeometry() { }

    public record PhotoQuad(float[] points,float[] uv) { }

    public static List<PhotoQuad> build(FrameWorkbenchMeshGeometry.Mesh mesh,
            int rotation,int extraRotation,float imagePixelWidth,float imagePixelHeight,
            boolean cover,int zoomPercent,int offsetX,int offsetY) {
        if(mesh==null || mesh.window().isEmpty())return List.of();
        int turn=(rotation+extraRotation)&3;
        float ratio=imagePixelWidth/Math.max(1.0F,imagePixelHeight);
        if(!Float.isFinite(ratio) || ratio<=0)ratio=1;
        if((turn&1)!=0)ratio=1/ratio;
        var window=mesh.windowBounds();
        float ww=window.x1()-window.x0(),wh=window.y1()-window.y0();
        if(!Float.isFinite(ww) || !Float.isFinite(wh) || ww<=0 || wh<=0)return List.of();
        float scale=cover ? Math.max(ww/ratio,wh) : Math.min(ww/ratio,wh);
        scale*=zoomPercent/100.0F;
        float imageWidth=ratio*scale,imageHeight=scale;
        if(!Float.isFinite(imageWidth) || !Float.isFinite(imageHeight)
                || imageWidth<=0 || imageHeight<=0)return List.of();
        float cx=(window.x0()+window.x1())/2+offsetX/100.0F*ww;
        float cy=(window.y0()+window.y1())/2+offsetY/100.0F*wh;
        float ix0=cx-imageWidth/2,iy0=cy-imageHeight/2;
        float ix1=cx+imageWidth/2,iy1=cy+imageHeight/2;
        List<PhotoQuad> quads=new ArrayList<>();
        for(var rect:mesh.window()) {
            float x0=Math.max(ix0,rect.x0()),x1=Math.min(ix1,rect.x1());
            float y0=Math.max(iy0,rect.y0()),y1=Math.min(iy1,rect.y1());
            if(x0>=x1 || y0>=y1)continue;
            float[] xyz={
                    x0,y0,mesh.front()-0.001F,
                    x1,y0,mesh.front()-0.001F,
                    x1,y1,mesh.front()-0.001F,
                    x0,y1,mesh.front()-0.001F
            };
            float[] u={(x0-ix0)/imageWidth,(x1-ix0)/imageWidth,(x1-ix0)/imageWidth,(x0-ix0)/imageWidth};
            float[] v={(iy1-y0)/imageHeight,(iy1-y0)/imageHeight,(iy1-y1)/imageHeight,(iy1-y1)/imageHeight};
            float[] uv=new float[8];
            for(int i=0;i<4;i++) {
                float a=u[i],b=v[i],ru=a,rv=b;
                switch(turn) {
                    case 1 -> {ru=b;rv=1-a;}
                    case 2 -> {ru=1-a;rv=1-b;}
                    case 3 -> {ru=1-b;rv=a;}
                    default -> { }
                }
                uv[i*2]=ru;
                uv[i*2+1]=rv;
            }
            quads.add(new PhotoQuad(xyz,uv));
        }
        return List.copyOf(quads);
    }
}
