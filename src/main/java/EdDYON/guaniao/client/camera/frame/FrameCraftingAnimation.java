package EdDYON.guaniao.client.camera.frame;

import net.minecraft.util.Mth;

/** Deterministic workshop visitor flight and curiosity timing, independent of GUI rendering. */
final class FrameCraftingAnimation {
    private FrameCraftingAnimation() { }

    static ObserverPose observerPose(float progressTicks,long seed,float exitTicks,
                                     float viewportLeft,float viewportTop,float viewportRight,float viewportBottom) {
        float ticks=sanitizeTicks(progressTicks);
        // Reserve the bottom of the preview for the entire wing/tail envelope.
        if(ticks<28)return flight(ticks/28,viewportLeft-64,112,viewportLeft+24,106,102,140,106,172,-65,-55);
        if(ticks<62)return curious(ticks-28,seed,106,172,-55);
        if(ticks<68)return launch(curious(34,seed,106,172,-55),ticks-62,-65);
        if(ticks<108)return flight(phase(ticks,68,108),106,172,126,166,184,166,204,172,-65,55);
        if(ticks<149)return curious(ticks-108,seed+3,204,172,55);
        if(ticks<155)return launch(curious(41,seed+3,204,172,55),ticks-149,65);
        if(ticks<198)return flight(phase(ticks,155,198),204,172,184,164,126,164,106,172,65,-55);
        if(ticks<231)return curious(ticks-198,seed+6,106,172,-55);
        if(ticks<237)return launch(curious(33,seed+6,106,172,-55),ticks-231,-65);
        if(ticks<272)return flight(phase(ticks,237,272),106,172,122,166,188,166,204,172,-65,55);
        ObserverPose watching=curious(Math.min(ticks,300)-272,seed+9,204,172,55-35*ease(phase(ticks,280,300)));
        if(ticks<300)return watching;
        float departure=Float.isFinite(exitTicks) ? Math.max(0,exitTicks) : 0;
        if(departure<6)return launch(watching,departure,-65);
        // Six ticks of wingbeats precede travel. The entire larger bird clears the real screen.
        return flight(phase(departure,6,24),204,172,245,144,viewportRight-20,viewportTop+80,
                viewportRight+64,viewportTop-64,-65,-65);
    }

    private static ObserverPose curious(float ticks,long seed,float x,float y,float yaw) {
        float offset=(seed&15)*.31F;
        float glance=Mth.sin(ticks*.14F+offset),tilt=pulse(ticks,5,23)-pulse(ticks,26,42);
        float settled=ease(phase(ticks,0,6));
        return new ObserverPose(x,y,yaw,Mth.sin(ticks*.11F+offset)*2.2F*settled,false,
                glance*17*settled,(-7-6*pulse(ticks,9,31))*settled,
                tilt*((seed&1)==0 ? 11 : -11)*settled);
    }

    private static ObserverPose launch(ObserverPose from,float ticks,float yaw) {
        float t=ease(phase(ticks,0,6));
        return new ObserverPose(from.x,from.y,Mth.rotLerp(t,from.yaw,yaw),from.lean*(1-t),true,
                from.lookYaw*(1-t),Mth.lerp(t,from.lookPitch,-3),from.lookRoll*(1-t));
    }

    /** Smooth start/landing, with a small bank and an inward turn as the bird settles. */
    private static ObserverPose flight(float progress,float x0,float y0,float x1,float y1,float x2,float y2,
                                       float x3,float y3,float yaw,float landingYaw) {
        float t=ease(Mth.clamp(progress,0,1)),inverse=1-t;
        float x=inverse*inverse*inverse*x0+3*inverse*inverse*t*x1+3*inverse*t*t*x2+t*t*t*x3;
        float y=inverse*inverse*inverse*y0+3*inverse*inverse*t*y1+3*inverse*t*t*y2+t*t*t*y3;
        float settle=ease(phase(progress,.82F,1));
        float bank=Mth.sin(progress*(float)Math.PI)*(x3>x0 ? -9 : 9);
        return new ObserverPose(x,y,Mth.rotLerp(settle,yaw,landingYaw),bank,true,0,-3*(1-settle),0);
    }

    record ObserverPose(float x,float y,float yaw,float lean,boolean flying,
                        float lookYaw,float lookPitch,float lookRoll) { }

    private static float sanitizeTicks(float ticks) {return Float.isFinite(ticks) ? Mth.clamp(ticks,0,300) : 0;}
    private static float phase(float ticks,float start,float end) {return Mth.clamp((ticks-start)/(end-start),0,1);}
    private static float ease(float t) {return t*t*(3-2*t);}
    private static float pulse(float ticks,float start,float end) {
        if(ticks<=start || ticks>=end)return 0;
        float wave=Mth.sin(phase(ticks,start,end)*(float)Math.PI);return wave*wave;
    }
}
