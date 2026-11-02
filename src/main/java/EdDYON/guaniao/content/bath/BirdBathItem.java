package EdDYON.guaniao.content.bath;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import org.apache.commons.lang3.mutable.MutableObject;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class BirdBathItem extends BlockItem implements GeoItem {
    private final BirdBathVariant variant;
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    // Populated by the Fabric client entrypoint; the common item never loads a client renderer.
    public final MutableObject<GeoRenderProvider> geoRenderProvider = new MutableObject<>();

    public BirdBathItem(BirdBathVariant variant, Block block, Properties properties) {
        super(block, properties);
        this.variant = variant;
    }

    public BirdBathVariant variant() {
        return this.variant;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(this.geoRenderProvider.getValue());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }
}
