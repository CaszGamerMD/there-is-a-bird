import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/** Isolated 26.2 NBT/data-component API compile proof. Not a second serializer. */
public final class GuaniaoFrameDesignComponentProof {
    private GuaniaoFrameDesignComponentProof() { }

    public static CompoundTag read(ItemStack stack) {
        CompoundTag custom=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        return custom.get("FrameDesign") instanceof CompoundTag nested ? nested : null;
    }

    public static void write(ItemStack stack,CompoundTag design) {
        CustomData.update(DataComponents.CUSTOM_DATA,stack,
                custom->custom.put("FrameDesign",design));
    }

    public static boolean safeForWorkbench(ItemStack stack, int maxBytes) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null || data.copyTag().sizeInBytes() <= maxBytes;
    }

    public static void copyPhotoReference(ItemStack source, ItemStack photograph) {
        CustomData data = source.get(DataComponents.CUSTOM_DATA);
        if (data != null) photograph.set(DataComponents.CUSTOM_DATA, data);
    }

    public static int version(CompoundTag tag) {return tag.getIntOr("Version",0);}

    public static boolean hasMask(CompoundTag tag) {
        Tag mask=tag.get("Mask");
        return mask!=null && mask.getId()==Tag.TAG_INT_ARRAY;
    }

    public static Identifier parseMaterial(String id) {return Identifier.tryParse(id);}
}
