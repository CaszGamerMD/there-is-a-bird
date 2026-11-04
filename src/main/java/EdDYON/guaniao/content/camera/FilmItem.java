package EdDYON.guaniao.content.camera;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class FilmItem extends Item {
    public FilmItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        // Legacy photo pixels and references are migrated only on the server.
        LegacyPhotoMigration.queue(level, stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() && PhotographData.hasImage(stack)) {
            ItemStack copy = stack.copy();
            // Keep the preview screen off the dedicated-server classpath.
            try {
                Class.forName("EdDYON.guaniao.client.camera.PhotographClientActions")
                        .getMethod("openScreen", ItemStack.class)
                        .invoke(null, copy);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Failed to open film preview", exception);
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        if (!PhotographData.hasImage(stack)) {
            tooltip.accept(Component.translatable("item.guaniao.film.tooltip.empty").withStyle(ChatFormatting.GRAY));
            return;
        }

        String photographer = PhotographData.photographer(stack);
        if (!photographer.isEmpty()) {
            tooltip.accept(Component.translatable("item.guaniao.photograph.tooltip.photographer",
                    PhotoAuthorText.component(photographer)).withStyle(ChatFormatting.GRAY));
        }
        tooltip.accept(Component.translatable("item.guaniao.film.tooltip.frame").withStyle(ChatFormatting.DARK_GRAY));
    }
}
