package EdDYON.guaniao.fabric;

import java.util.List;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WrittenBookContent;

/** Compile-only proof of 26.2 written books and server advancement API. */
public final class GuaniaoNoteComponentsProof {
    private GuaniaoNoteComponentsProof() { }

    public static ItemStack authoredNote(String title, String author, String key) {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT,
                new WrittenBookContent(Filterable.passThrough(title), author, 0,
                        List.of(Filterable.passThrough(Component.translatable(key))), true));
        CompoundTag data = new CompoundTag();
        data.putByte("GuaniaoNote", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    public static boolean isReadNote(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        return data != null && content != null && !content.author().isEmpty()
                && data.copyTag().getByte("GuaniaoNote").orElse((byte) 0) == 1;
    }

    public static void award(ServerPlayer player, Identifier advancementId) {
        AdvancementHolder holder = player.getServer().getAdvancements().get(advancementId);
        if (holder != null) {
            player.getAdvancements().award(holder, "triggered");
        }
    }
}
