package EdDYON.guaniao.proof;

import java.util.function.Consumer;
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

/** Compile the actual Fabric 26.2 film inventory, use and tooltip API signatures. */
public final class GuaniaoFilmItemProof extends Item {
    public GuaniaoFilmItemProof(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.guaniao.film.tooltip.empty"));
    }
}
