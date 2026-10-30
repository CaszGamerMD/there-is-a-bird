package EdDYON.guaniao.fabric;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Compile-only GeckoLib 5.5.5 proof for food attached to animated bird bones. */
public final class GuaniaoHeldItemRenderProof {
    private GuaniaoHeldItemRenderProof() { }

    public static BlockAndItemGeoLayer.RenderData prepare(ItemStack item,
                                                            ItemModelResolver resolver,
                                                            LivingEntity bird,
                                                            String boneName,
                                                            ItemDisplayContext context) {
        ItemStackRenderState state =
                RenderUtil.createRenderStateForItem(item, resolver, context, bird);
        return BlockAndItemGeoLayer.RenderData.item(boneName, context, state);
    }

    public static <R extends GeoRenderState> void submit(RenderPassInfo<R> pass,
                                                          GeoBone bone,
                                                          SubmitNodeCollector collector,
                                                          ItemStackRenderState item) {
        // A per-bone callback already has the animated bone's pose on this stack.
        item.submit(pass.poseStack(), collector, pass.packedLight(), pass.packedOverlay(), 0);
    }
}
