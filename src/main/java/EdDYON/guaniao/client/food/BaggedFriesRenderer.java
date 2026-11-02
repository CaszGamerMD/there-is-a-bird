package EdDYON.guaniao.client.food;

import EdDYON.guaniao.content.food.BaggedFriesBlockEntity;
import EdDYON.guaniao.content.food.BaggedFriesBlock;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public final class BaggedFriesRenderer extends GeoBlockRenderer<BaggedFriesBlockEntity, BlockEntityRenderState> {
    private static final DataTicket<Integer> FRY_MASK =
            DataTicket.create("guaniao_fry_mask", Integer.class);

    public BaggedFriesRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new BaggedFriesModel());
    }

    @Override
    public void addRenderData(BaggedFriesBlockEntity fries, Void unused, BlockEntityRenderState state, float partialTick) {
        super.addRenderData(fries, unused, state, partialTick);
        state.addGeckolibData(FRY_MASK, fries.getFryMask());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<BlockEntityRenderState> info, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(info, snapshots);
        int mask = info.getOrDefaultGeckolibData(FRY_MASK, 0);
        for (int index = 1; index <= BaggedFriesBlock.TOTAL_FRIES; index++) {
            boolean visible = (mask & (1 << (index - 1))) != 0;
            snapshots.ifPresent("fry_" + String.format("%02d", index), bone -> {
                bone.skipRender(!visible);
                bone.skipChildrenRender(!visible);
            });
        }
    }

    private static int fryIndex(String name) {
        if (!name.startsWith("fry_") || name.length() != 6) {
            return 0;
        }
        try {
            int index = Integer.parseInt(name.substring(4));
            return index >= 1 && index <= BaggedFriesBlock.TOTAL_FRIES ? index : 0;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
