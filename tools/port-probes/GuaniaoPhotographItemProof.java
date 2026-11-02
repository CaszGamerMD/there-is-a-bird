package EdDYON.guaniao.proof;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Compile the 26.2 photo preview, wall placement, material fallback and tooltip hooks. */
public final class GuaniaoPhotographItemProof extends Item {
    public GuaniaoPhotographItemProof(Properties properties) {
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
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
        if (player == null || !player.mayBuild() || !context.getLevel().mayInteract(player, placePos)
                || !player.mayUseItemAt(placePos, Direction.UP, context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        return context.getLevel().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        Item material = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", "oak_planks"));
        tooltip.accept(material == null ? Component.literal("unknown") : new ItemStack(material).getHoverName());
    }
}
