import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Standalone Fabric 26.2 API compile check for the two private GUI menus.
 * Test only signatures, not unported CrowNest or FrameWorkbench gameplay.
 */
public final class GuaniaoExtendedMenuProof {
    private static final StreamCodec<RegistryFriendlyByteBuf, BlockPos> POSITION =
            StreamCodec.of((buffer, pos) -> buffer.writeBlockPos(pos),
                    RegistryFriendlyByteBuf::readBlockPos);

    private GuaniaoExtendedMenuProof() {}

    public static <T extends AbstractContainerMenu> ExtendedMenuType<T, BlockPos> type(
            ExtendedMenuType.ExtendedFactory<T, BlockPos> factory) {
        return new ExtendedMenuType<>(factory, POSITION);
    }

    public static BlockPos openingData(ExtendedMenuProvider<BlockPos> provider,
                                       ServerPlayer player) {
        return provider.getScreenOpeningData(player);
    }
}
