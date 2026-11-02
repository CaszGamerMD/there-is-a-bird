package EdDYON.guaniao.client.nest;

import EdDYON.guaniao.content.nest.CrowNestBlockEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class CrowNestRenderer extends GeoBlockRenderer<CrowNestBlockEntity, BlockEntityRenderState> {
    private static final DataTicket<Integer> EGGS =
            DataTicket.create("guaniao_nest_eggs", Integer.class);

    public CrowNestRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new CrowNestModel());
    }

    @Override
    public void addRenderData(CrowNestBlockEntity nest, Void unused, BlockEntityRenderState state, float partialTick) {
        super.addRenderData(nest, unused, state, partialTick);
        state.addGeckolibData(EGGS, nest.getEggCount());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<BlockEntityRenderState> info, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(info, snapshots);
        int eggs = info.getOrDefaultGeckolibData(EGGS, 0);
        for (int index = 1; index <= 3; index++) {
            boolean visible = eggs >= index;
            snapshots.ifPresent("egg" + index, bone -> {
                bone.skipRender(!visible);
                bone.skipChildrenRender(!visible);
            });
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
