package EdDYON.guaniao.content.nest;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.mutable.MutableObject;

public class CrowNestItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    // The client initializer supplies the renderer; common gameplay remains server-safe.
    public final MutableObject<GeoRenderProvider> geoRenderProvider = new MutableObject<>();

    public CrowNestItem(Block block, Properties properties) {
        super(block, properties);
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
