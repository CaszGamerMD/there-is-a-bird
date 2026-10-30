package EdDYON.guaniao.client.camera;

/** Standalone Java test of the original GUI eyepiece to JPEG pixel rectangle. */
public final class GuaniaoViewfinderFrameRegression {
    private GuaniaoViewfinderFrameRegression() { }
    private static void eq(int a, int b, String what) {
        if (a!=b) throw new AssertionError(what + ": " + a + " != " + b);
    }
    private static void test(int w,int h,int line,int pw,int ph) {
        var frame=CameraViewfinderFrame.layout(w,h,line);
        int margin=line*2+18;
        int expected=Math.max(1,Math.min(Math.min(w-24,h*80/100),Math.max(1,h-2*margin)));
        eq(frame.size(),expected,"author size");
        eq(frame.left(),(w-expected)/2,"author left");
        eq(frame.top(),(h-expected)/2,"author top");
        var physical=frame.pixels(w,h,pw,ph);
        if(physical.width()<1||physical.height()<1||physical.left()<0||physical.top()<0
                ||physical.left()+physical.width()>pw||physical.top()+physical.height()>ph)
            throw new AssertionError("Out of framebuffer");
        var identity=frame.pixels(w,h,w,h);
        eq(identity.left(),frame.left(),"identity left");
        eq(identity.top(),frame.top(),"identity top");
        eq(identity.width(),frame.size(),"identity width");
        eq(identity.height(),frame.size(),"identity height");
    }
    public static void main(String[] args) {
        test(854,480,9,1708,960);
        test(960,540,9,1920,1080);
        test(1280,720,9,1920,1080);
        test(1920,1080,9,3840,2160);
        test(426,240,9,1704,960);
        test(240,180,9,960,720);
        System.out.println("PASS: authored viewfinder geometry and captured-photo dimensions");
    }
}
