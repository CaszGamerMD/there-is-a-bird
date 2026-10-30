package EdDYON.guaniao.registry;

import EdDYON.guaniao.content.nest.CrowNestMenu;
import EdDYON.guaniao.content.camera.frame.FrameWorkbenchMenu;
import EdDYON.guaniao.fabric.FabricRegistryQueue;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.MenuType;

/**
 * Keep the original two menu IDs and send the exact block position on open.
 * Fabric 26.2 ExtendedMenuType retains block-position opening without removing
 * the server-authoritative nest search or persistent frame workbench jobs.
 */
public final class GuaniaoMenuTypes {
    public static final FabricRegistryQueue<MenuType<?>> MENU_TYPES =
            new FabricRegistryQueue<>(BuiltInRegistries.MENU, "guaniao");

    private static final StreamCodec<RegistryFriendlyByteBuf, BlockPos> BLOCK_POSITION =
            StreamCodec.of((buffer, pos) -> buffer.writeBlockPos(pos),
                    RegistryFriendlyByteBuf::readBlockPos);

    public static final FabricRegistryQueue.Ref<MenuType<CrowNestMenu>> CROW_NEST =
            MENU_TYPES.register("crow_nest", () -> new ExtendedMenuType<>(
                    (containerId, inventory, pos) -> new CrowNestMenu(containerId, inventory, pos),
                    BLOCK_POSITION));

    public static final FabricRegistryQueue.Ref<MenuType<FrameWorkbenchMenu>> FRAME_WORKBENCH =
            MENU_TYPES.register("frame_workbench", () -> new ExtendedMenuType<>(
                    (containerId, inventory, pos) -> new FrameWorkbenchMenu(containerId, inventory, pos),
                    BLOCK_POSITION));

    private GuaniaoMenuTypes() { }
}
