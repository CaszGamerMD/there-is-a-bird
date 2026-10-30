package EdDYON.guaniao.client.camera;

/** Standalone Java regression for original full lens UVs and square thumbnails. */
public final class GuaniaoCameraPreviewUvRegression {
    private GuaniaoCameraPreviewUvRegression() { }
    private static void eq(float actual,float expected,String label) {
        if (!Float.isFinite(actual)||Math.abs(actual-expected)>0.000001F)
            throw new AssertionError(label+": "+actual+" != "+expected);
    }
    private static void check(CameraPreviewUv.Uv uv,float x0,float y0,float x1,float y1) {
        eq(uv.u0(),x0,"U0");
        eq(uv.vBottom(),y0,"bottom V");
        eq(uv.u1(),x1,"U1");
        eq(uv.vTop(),y1,"top V");
    }
    public static void main(String[] args) {
        check(CameraPreviewUv.lens(25,20,75,80,100,100),0.25F,0.20F,0.75F,0.80F);
        check(CameraPreviewUv.lens(0,0,1920,1080,1920,1080),0F,0F,1F,1F);
        check(CameraPreviewUv.centerCrop(1920,1080,256,256),0.21875F,0F,0.78125F,1F);
        check(CameraPreviewUv.centerCrop(1024,1024,320,180),0F,0.21875F,1F,0.78125F);
        check(CameraPreviewUv.centerCrop(1920,1080,320,180),0F,0F,1F,1F);
        check(CameraPreviewUv.centerCrop(1024,1024,1024,1024),0F,0F,1F,1F);
        try {
            CameraPreviewUv.lens(0,0,0,1,1,1);
            throw new AssertionError("Zero-width lens accepted");
        } catch (IllegalArgumentException expected) { }
        try {
            CameraPreviewUv.centerCrop(1920,0,100,100);
            throw new AssertionError("Invalid source height accepted");
        } catch (IllegalArgumentException expected) { }
        System.out.println("PASS: 26.2 camera lens and original centered thumbnail UVs");
    }
}
