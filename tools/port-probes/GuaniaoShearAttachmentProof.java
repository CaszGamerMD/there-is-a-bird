package EdDYON.guaniao.fabric;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Compile-only proof: entity shearing, persistent attachments and hand tool wear on Fabric 26.2. */
public final class GuaniaoShearAttachmentProof {
    private static final AttachmentType<Long> LAST = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("guaniao", "proof_shear"),
            builder -> builder.persistent(Codec.LONG));

    private GuaniaoShearAttachmentProof() {
    }

    public static InteractionResult onUse(Player player, Level level, InteractionHand hand, Entity target) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Long previous = target.getAttached(LAST);
        long now = level.getGameTime();
        if (previous != null && now - previous >= 0 && now - previous < 6000L) {
            return InteractionResult.SUCCESS_SERVER;
        }
        target.setAttached(LAST, now);
        long saved = target.getAttachedOrElse(LAST, 0L);
        if (saved < 0L) {
            target.removeAttached(LAST);
        }
        player.getItemInHand(hand).hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS_SERVER;
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, level, hand, target, hitResult) ->
                onUse(player, level, hand, target));
    }
}
