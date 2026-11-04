package EdDYON.guaniao.client.camera.frame;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** 26.2 queued photograph quad emission, retaining original per-window clipping. */
final class FrameWorkbenchPhotoSubmitter {
    private FrameWorkbenchPhotoSubmitter() { }

    static void submit(List<FrameWorkbenchPhotoGeometry.PhotoQuad> quads,
            Identifier texture,PoseStack pose,SubmitNodeCollector collector,
            int packedLight,float alpha) {
        if(quads.isEmpty() || texture==null || alpha<=0)return;
        collector.submitCustomGeometry(pose,RenderTypes.text(texture),(snapshot,out)->{
            for(var quad:quads)for(int i=0;i<4;i++) {
                int p=i*3,t=i*2;
                out.addVertex(snapshot,quad.points()[p],quad.points()[p+1],quad.points()[p+2])
                        .setColor(1.0F,1.0F,1.0F,alpha)
                        .setUv(quad.uv()[t],quad.uv()[t+1])
                        .setLight(packedLight);
            }
        });
    }
}
