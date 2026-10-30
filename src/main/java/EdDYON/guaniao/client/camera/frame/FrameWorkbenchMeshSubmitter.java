package EdDYON.guaniao.client.camera.frame;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Minecraft 26.2 deferred geometry submission. Replays EXACT original tiled
 * frame quads with the sprite's normalized UVs, color and face normals.
 *
 * Previously TextureAtlasSprite.getU/getV accepted 0..16 sprite pixels.
 * In 26.2 they accept 0..1 fractional sprite coordinates. The pure builder's
 * UVs already lie in 0..1 because its original tile() split each face on
 * 1-block boundaries, so no extra *16 may be applied.
 */
final class FrameWorkbenchMeshSubmitter {
    private FrameWorkbenchMeshSubmitter() { }

    static void submit(List<FrameWorkbenchMeshGeometry.Quad> quads,
                       TextureAtlasSprite sprite, PoseStack pose,
                       SubmitNodeCollector collector, int packedLight,
                       float red, float green, float blue, float alpha,
                       boolean translucent) {
        if (quads.isEmpty() || sprite == null || alpha <= 0) return;
        var type=translucent || alpha<1
                ? RenderTypes.entityTranslucent(sprite.atlasLocation())
                : RenderTypes.entityCutout(sprite.atlasLocation());
        // SubmitNodeStorage snapshots the matrix NOW. This callback executes
        // only later when the feature render dispatcher flushes all submissions.
        collector.submitCustomGeometry(pose,type,(snapshot,out)->{
            for(FrameWorkbenchMeshGeometry.Quad quad:quads) {
                for(int i=0;i<4;i++) {
                    int p=i*3,uv=i*2;
                    out.addVertex(snapshot,quad.points()[p],quad.points()[p+1],quad.points()[p+2])
                            .setColor(red,green,blue,alpha)
                            .setUv(sprite.getU(quad.uv()[uv]),sprite.getV(quad.uv()[uv+1]))
                            .setLight(packedLight)
                            .setNormal(snapshot,quad.nx(),quad.ny(),quad.nz());
                }
            }
        });
    }
}
