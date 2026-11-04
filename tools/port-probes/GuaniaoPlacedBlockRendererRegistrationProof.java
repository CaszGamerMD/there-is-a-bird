package EdDYON.guaniao.proof;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Compile Fabric's real client block-entity renderer registration contract. */
public final class GuaniaoPlacedBlockRendererRegistrationProof {
    private GuaniaoPlacedBlockRendererRegistrationProof() {}

    public static <T extends BlockEntity, S extends BlockEntityRenderState> void register(BlockEntityType<T> type,
                                                        BlockEntityRendererProvider<T, S> provider) {
        BlockEntityRendererRegistry.register(type, provider);
    }
}
