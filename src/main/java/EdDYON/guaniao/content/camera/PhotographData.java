package EdDYON.guaniao.content.camera;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class PhotographData {
    public static final int IMAGE_SIZE = PhotoTransferLimits.IMAGE_WIDTH;
    public static final int LEGACY_IMAGE_SIZE = PhotoTransferLimits.LEGACY_IMAGE_WIDTH;
    private static final int MIN_IMAGE_SIZE = 16;
    private static final int MAX_IMAGE_SIZE = PhotoTransferLimits.IMAGE_WIDTH;
    public static final String TAG_PHOTO_ID = "PhotoId";
    public static final String TAG_PHOTOGRAPHER = "Photographer";
    public static final String TAG_PHOTOGRAPHER_ID = "PhotographerId";
    public static final String TAG_GAME_TIME = "GameTime";
    public static final String TAG_WIDTH = "Width";
    public static final String TAG_HEIGHT = "Height";
    public static final String TAG_CONTENT_HASH = "ContentHash";
    public static final String TAG_PIXELS = "Pixels";

    private PhotographData() {
    }

    private static CompoundTag photoTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static int[] legacyPixels(CompoundTag tag) {
        return tag.getIntArray(TAG_PIXELS).orElseGet(() -> new int[0]);
    }

    public static boolean hasImage(ItemStack stack) {
        CompoundTag tag = photoTag(stack);
        return tag.contains(TAG_PHOTO_ID)
                && PhotoTransferLimits.isValidPhotoId(tag.getStringOr(TAG_PHOTO_ID, ""))
                && imageWidth(tag) > 0
                && imageHeight(tag) > 0;
    }

    public static String id(ItemStack stack) {
        return photoTag(stack).getStringOr(TAG_PHOTO_ID, "");
    }

    public static String photographer(ItemStack stack) {
        return photoTag(stack).getStringOr(TAG_PHOTOGRAPHER, "");
    }

    public static UUID photographerId(ItemStack stack) {
        return photoTag(stack).read(TAG_PHOTOGRAPHER_ID, UUIDUtil.CODEC).orElse(null);
    }

    public static long gameTime(ItemStack stack) {
        return photoTag(stack).getLongOr(TAG_GAME_TIME, 0L);
    }

    public static int[] pixels(ItemStack stack) {
        return legacyPixels(photoTag(stack));
    }

    public static boolean hasLegacyPixels(ItemStack stack) {
        return legacyPixels(photoTag(stack)).length == LEGACY_IMAGE_SIZE * LEGACY_IMAGE_SIZE;
    }

    public static String contentHash(ItemStack stack) {
        return photoTag(stack).getStringOr(TAG_CONTENT_HASH, "");
    }

    public static int width(ItemStack stack) {
        return imageWidth(photoTag(stack));
    }

    public static int height(ItemStack stack) {
        return imageHeight(photoTag(stack));
    }

    public static void writeReference(
            ItemStack stack,
            String id,
            String photographer,
            UUID photographerId,
            long gameTime,
            int width,
            int height,
            String contentHash
    ) {
        if (!PhotoTransferLimits.isValidPhotoId(id)
                || width < MIN_IMAGE_SIZE
                || height < MIN_IMAGE_SIZE
                || width > MAX_IMAGE_SIZE
                || height > MAX_IMAGE_SIZE
                || !PhotoImageCodec.isSha256(contentHash)) {
            throw new IllegalArgumentException("Invalid photograph reference");
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(TAG_PHOTO_ID, id);
            tag.putString(TAG_PHOTOGRAPHER, photographer);
            if (photographerId == null) tag.remove(TAG_PHOTOGRAPHER_ID);
            else tag.store(TAG_PHOTOGRAPHER_ID, UUIDUtil.CODEC, photographerId);
            tag.putLong(TAG_GAME_TIME, gameTime);
            tag.putInt(TAG_WIDTH, width);
            tag.putInt(TAG_HEIGHT, height);
            tag.putString(TAG_CONTENT_HASH, contentHash);
            tag.remove(TAG_PIXELS);
        });
    }

    public static void copyImage(ItemStack from, ItemStack to) {
        CustomData sourceData = from.get(DataComponents.CUSTOM_DATA);
        if (sourceData == null) {
            return;
        }
        CompoundTag source = sourceData.copyTag();

        CustomData.update(DataComponents.CUSTOM_DATA, to, target -> {
            if (source.contains(TAG_PHOTO_ID)) {
                target.putString(TAG_PHOTO_ID, source.getStringOr(TAG_PHOTO_ID, ""));
            }
            if (source.contains(TAG_PHOTOGRAPHER)) {
                target.putString(TAG_PHOTOGRAPHER, source.getStringOr(TAG_PHOTOGRAPHER, ""));
            }
            source.read(TAG_PHOTOGRAPHER_ID, UUIDUtil.CODEC)
                    .ifPresentOrElse(id -> target.store(TAG_PHOTOGRAPHER_ID, UUIDUtil.CODEC, id),
                            () -> target.remove(TAG_PHOTOGRAPHER_ID));
            if (source.contains(TAG_GAME_TIME)) {
                target.putLong(TAG_GAME_TIME, source.getLongOr(TAG_GAME_TIME, 0L));
            }
            if (source.contains(TAG_CONTENT_HASH)) {
                target.putString(TAG_CONTENT_HASH, source.getStringOr(TAG_CONTENT_HASH, ""));
            }
            int width = imageWidth(source);
            int height = imageHeight(source);
            target.putInt(TAG_WIDTH, width > 0 ? width : LEGACY_IMAGE_SIZE);
            target.putInt(TAG_HEIGHT, height > 0 ? height : LEGACY_IMAGE_SIZE);
            // Legacy pixels are copied only so an old film crafted before migration can
            // still be imported by the resulting photograph item on its next server tick.
            if (legacyPixels(source).length == LEGACY_IMAGE_SIZE * LEGACY_IMAGE_SIZE) {
                target.putIntArray(TAG_PIXELS, legacyPixels(source));
            }
        });
    }

    public static void finishLegacyMigration(ItemStack stack, String contentHash) {
        if (stack.get(DataComponents.CUSTOM_DATA) == null || !PhotoImageCodec.isSha256(contentHash)) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(TAG_WIDTH, LEGACY_IMAGE_SIZE);
            tag.putInt(TAG_HEIGHT, LEGACY_IMAGE_SIZE);
            tag.putString(TAG_CONTENT_HASH, contentHash);
            tag.remove(TAG_PIXELS);
        });
    }

    private static int imageWidth(CompoundTag tag) {
        int width = tag.getIntOr(TAG_WIDTH, 0);
        int height = tag.getIntOr(TAG_HEIGHT, 0);
        if (validDimensions(width, height)) {
            return width;
        }

        return legacyPixels(tag).length == LEGACY_IMAGE_SIZE * LEGACY_IMAGE_SIZE ? LEGACY_IMAGE_SIZE : 0;
    }

    private static int imageHeight(CompoundTag tag) {
        int width = tag.getIntOr(TAG_WIDTH, 0);
        int height = tag.getIntOr(TAG_HEIGHT, 0);
        if (validDimensions(width, height)) {
            return height;
        }

        return legacyPixels(tag).length == LEGACY_IMAGE_SIZE * LEGACY_IMAGE_SIZE ? LEGACY_IMAGE_SIZE : 0;
    }

    private static boolean validDimensions(int width, int height) {
        return width >= MIN_IMAGE_SIZE
                && height >= MIN_IMAGE_SIZE
                && width <= MAX_IMAGE_SIZE
                && height <= MAX_IMAGE_SIZE;
    }
}
