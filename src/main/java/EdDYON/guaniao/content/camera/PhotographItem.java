package EdDYON.guaniao.content.camera;

import EdDYON.guaniao.content.camera.frame.FrameDesign;
import java.math.BigDecimal;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public class PhotographItem extends Item {
    public PhotographItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        LegacyPhotoMigration.queue(level, stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() && PhotographData.hasImage(stack)) {
            ItemStack copy = stack.copy();
            // The photo viewer belongs to the client only, never the dedicated server.
            try {
                Class.forName("EdDYON.guaniao.client.camera.PhotographClientActions")
                        .getMethod("openScreen", ItemStack.class)
                        .invoke(null, copy);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Failed to open photograph screen", exception);
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        Direction face = context.getClickedFace();
        ItemStack stack = context.getItemInHand();

        if (player == null || (!PhotographData.hasImage(stack) && FrameDesign.read(stack) == null) || !face.getAxis().isHorizontal()) {
            return InteractionResult.PASS;
        }

        FrameDesign placementDesign = FrameDesign.read(stack);
        if (placementDesign != null && !placementDesign.hasArea()) return InteractionResult.FAIL;
        BlockPos placePos = context.getClickedPos().relative(face);
        if (!player.mayBuild() || !level.mayInteract(player, placePos) || !player.mayUseItemAt(placePos, face, stack)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            LegacyPhotoMigration.migrateNow(level, stack);
            ItemStack placedPhoto = stack.copy();
            placedPhoto.setCount(1);
            LegacyPhotoMigration.queue(level, placedPhoto);
            PhotographEntity entity = new PhotographEntity(level, placePos, face, placedPhoto);
            if (!entity.survives()) {
                return InteractionResult.FAIL;
            }
            if (!level.addFreshEntity(entity)) {
                return InteractionResult.FAIL;
            }
            level.playSound(null, placePos, SoundEvents.ITEM_FRAME_PLACE, SoundSource.BLOCKS, 0.8F, 1.1F);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        FrameDesign design = FrameDesign.read(stack);
        if (design == null) {
            PhotographFrameStyle style = PhotographFrameStyle.read(stack);
            tooltip.accept(Component.translatable("item.guaniao.photograph.tooltip.style",
                    Component.translatable("frame.guaniao.shape." + style.shape().id()),
                    Component.translatable("frame.guaniao.material." + style.material().id()),
                    Component.translatable("frame.guaniao.ornament." + style.ornament().id())).withStyle(ChatFormatting.GRAY));
        } else {
            Item material = BuiltInRegistries.ITEM.getValue(design.frameMaterial());
            tooltip.accept(Component.translatable("item.guaniao.photograph.tooltip.design", blockSize(design.widthPixels()), blockSize(design.heightPixels()),
                    material != null ? new ItemStack(material).getHoverName()
                            : Component.literal(design.frameMaterial().toString())).withStyle(ChatFormatting.GRAY));
        }
        if (!PhotographData.hasImage(stack)) {
            tooltip.accept(Component.translatable("item.guaniao.photograph.tooltip.empty").withStyle(ChatFormatting.GRAY));
            return;
        }

        String photographer = PhotographData.photographer(stack);
        if (!photographer.isEmpty()) {
            tooltip.accept(Component.translatable("item.guaniao.photograph.tooltip.photographer",
                    PhotoAuthorText.component(photographer)).withStyle(ChatFormatting.GRAY));
        }
        tooltip.accept(Component.translatable("item.guaniao.photograph.tooltip.actions").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.translatable("item.guaniao.photograph.tooltip.wall").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static String blockSize(int pixels) {
        return BigDecimal.valueOf(pixels).divide(BigDecimal.valueOf(16)).stripTrailingZeros().toPlainString();
    }
}
