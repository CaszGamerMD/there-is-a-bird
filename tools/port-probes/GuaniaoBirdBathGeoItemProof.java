package EdDYON.guaniao.proof;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Compile the 26.2 GeckoLib 5 item-provider handoff without loading the legacy client renderer. */
public final class GuaniaoBirdBathGeoItemProof extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public final MutableObject<GeoRenderProvider> geoRenderProvider = new MutableObject<>();

    public GuaniaoBirdBathGeoItemProof(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(this.geoRenderProvider.getValue());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private static GeoRenderProvider proofClientProvider(
            Supplier<GeoItemRenderer<GuaniaoBirdBathGeoItemProof>> renderer) {
        return new GeoRenderProvider() {
            @Override
            public GeoItemRenderer<GuaniaoBirdBathGeoItemProof> getGeoItemRenderer() {
                return renderer.get();
            }
        };
    }
}
