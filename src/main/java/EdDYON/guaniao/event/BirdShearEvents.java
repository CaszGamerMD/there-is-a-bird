package EdDYON.guaniao.event;

import EdDYON.guaniao.config.BirdSpecies;
import EdDYON.guaniao.content.bird.flight.BirdFlightLock;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;

/**
 * Shearing: right-click a bird with shears to pluck one of its species' color feathers.
 * The feather needs time to regrow before the bird can be sheared again; mutations yield
 * the closest matching special color.
 */
public final class BirdShearEvents {
    private static final int REGROW_TICKS = 6000;
    private static final AttachmentType<Long> LAST_SHEAR_TIME =
            AttachmentRegistry.create(Identifier.fromNamespaceAndPath("guaniao", "last_shear_time"),
                    builder -> builder.persistent(Codec.LONG));

    private BirdShearEvents() {
    }

    public static InteractionResult onEntityInteract(Player player, Level level, InteractionHand hand, Entity target) {
        if (!(target instanceof LivingEntity bird) || BirdSpecies.from(bird) == null || player.isSpectator()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ShearsItem)) {
            return InteractionResult.PASS;
        }

        // The Fabric callback must also consume the client interaction so it sends
        // the vanilla use-entity packet; mutations and inventory effects remain server-only.
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Item feather = BirdFeatherEvents.randomShearFeatherFor(bird);
        if (feather == null) {
            return InteractionResult.SUCCESS_SERVER;
        }

        Long last = bird.getAttached(LAST_SHEAR_TIME);
        long now = level.getGameTime();
        if (last != null) {
            long elapsed = now - last;
            if (elapsed >= 0L && elapsed < REGROW_TICKS) {
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        bird.setAttached(LAST_SHEAR_TIME, now);
        BirdFeatherEvents.spawnFeather(bird, feather, 1);
        BirdFlightLock.disableFlight(bird, REGROW_TICKS);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS_SERVER;
    }

    public static void registerCallbacks() {
        UseEntityCallback.EVENT.register((player, level, hand, target, hitResult) ->
                onEntityInteract(player, level, hand, target));
    }
}
