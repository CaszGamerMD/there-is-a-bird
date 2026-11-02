package EdDYON.guaniao.client.cage;

import EdDYON.guaniao.content.cage.BirdCageBlockEntity;
import EdDYON.guaniao.content.cage.BirdCageVariant;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class BirdCageRenderer extends GeoBlockRenderer<BirdCageBlockEntity, BlockEntityRenderState> {
    public static final DataTicket<BirdCageVariant> VARIANT =
            DataTicket.create("guaniao_cage_variant", BirdCageVariant.class);

    public BirdCageRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new BirdCageModel());
    }

    @Override
    public void addRenderData(BirdCageBlockEntity cage, Void unused, BlockEntityRenderState state, float partialTick) {
        super.addRenderData(cage, unused, state, partialTick);
        state.addGeckolibData(VARIANT, cage.variant());
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
