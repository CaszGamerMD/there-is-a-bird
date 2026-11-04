package EdDYON.guaniao.mixin;

import EdDYON.guaniao.client.fan.FeatherFanHandPose;
import EdDYON.guaniao.content.fan.FeatherFanItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Apply the original Forge fan charge pose at the Fabric 26.2 item submission point. */
@Mixin(ItemInHandRenderer.class)
public abstract class FeatherFanHandPoseMixin {
    @Inject(method = "submitArmWithItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"),
            require = 1)
    private void guaniao$fanChargePose(AbstractClientPlayer player, float partialTick, float pitch,
                                       InteractionHand hand, float swingProgress, ItemStack stack,
                                       float equipProgress, PoseStack pose, SubmitNodeCollector collector,
                                       int light, CallbackInfo ci) {
        if (!(player instanceof LocalPlayer local) || !(stack.getItem() instanceof FeatherFanItem)) {
            return;
        }
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND
                ? local.getMainArm()
                : local.getMainArm().getOpposite();
        FeatherFanHandPose.apply(pose, local, arm, stack, partialTick, equipProgress);
    }
}
