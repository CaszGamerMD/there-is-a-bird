package EdDYON.guaniao.event;

import EdDYON.guaniao.content.advancement.BirdAdvancements;
import EdDYON.guaniao.content.note.BirdNoteContent;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WrittenBookContent;

/** Award the authored note-reading achievements on server-side book use. */
public final class BirdNoteEvents {
    private BirdNoteEvents() { }

    public static void registerCallbacks() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.WRITTEN_BOOK)) {
                return InteractionResult.PASS;
            }
            CustomData marker = stack.get(DataComponents.CUSTOM_DATA);
            if (marker == null || marker.copyTag().getByte(BirdNoteContent.NOTE_TAG).orElse((byte) 0) != 1) {
                return InteractionResult.PASS;
            }
            BirdAdvancements.grant(serverPlayer, BirdAdvancements.BIRD_NOTE);
            WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (content != null) {
                BirdAdvancements.awardDevNote(serverPlayer, content.author());
            }
            return InteractionResult.PASS;
        });
    }
}
