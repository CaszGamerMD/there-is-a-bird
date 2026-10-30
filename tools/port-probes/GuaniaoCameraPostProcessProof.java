package EdDYON.guaniao.fabric;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.renderer.PostChain;

/**
 * Compile-only 26.2 proof of the backend-neutral, private post-chain API.
 * PostChain.process is deprecated but still supported in this target version.
 */
public final class GuaniaoCameraPostProcessProof {
    private GuaniaoCameraPostProcessProof() { }

    @SuppressWarnings("deprecation")
    public static void apply(PostChain chain, RenderTarget privateTarget) {
        chain.process(privateTarget, GraphicsResourceAllocator.UNPOOLED);
    }
}
